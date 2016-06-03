/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.data.sql.impl.ClanTable;
import org.l2junity.gameserver.data.xml.impl.OneDayRewardData;
import org.l2junity.gameserver.model.ClanMember;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.OneDayRewardDataHolder;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.stat.PcStat;
import org.l2junity.gameserver.model.base.SubClass;
import org.l2junity.gameserver.model.eventengine.AbstractEvent;
import org.l2junity.gameserver.model.eventengine.AbstractEventManager;
import org.l2junity.gameserver.model.eventengine.ScheduleTarget;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.olympiad.Olympiad;
import org.l2junity.gameserver.model.variables.PlayerVariables;
import org.l2junity.gameserver.network.client.send.ExVoteSystemInfo;
import org.l2junity.gameserver.network.client.send.ExWorldChatCnt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class DailyTaskManager extends AbstractEventManager<AbstractEvent<?>>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyTaskManager.class);
	
	protected DailyTaskManager()
	{
	}
	
	@Override
	public void onInitialized()
	{
	}
	
	@ScheduleTarget
	private void onReset()
	{
		resetClanBonus();
		resetExtendDrop();
		resetDailySkills();
		resetWorldChatPoints();
		resetOneDayReward();
		resetRecommends();
	}
	
	@ScheduleTarget
	private void onSave()
	{
		GlobalVariablesManager.getInstance().storeMe();
		
		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			LOGGER.info("Olympiad System: Data updated.");
		}
	}
	
	@ScheduleTarget
	private void onClanLeaderApply()
	{
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() != 0)
			{
				final ClanMember member = clan.getClanMember(clan.getNewLeaderId());
				if (member == null)
				{
					continue;
				}
				
				clan.setNewLeader(member);
			}
		}
		LOGGER.info("Clan leaders has been updated");
	}
	
	@ScheduleTarget
	private void onVitalityReset()
	{
		for (PlayerInstance player : World.getInstance().getPlayers())
		{
			player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, false);
			
			for (SubClass subclass : player.getSubClasses().values())
			{
				subclass.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
			}
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("UPDATE character_subclasses SET vitality_points = ?"))
			{
				st.setInt(1, PcStat.MAX_VITALITY_POINTS);
				st.execute();
			}
			
			try (PreparedStatement st = con.prepareStatement("UPDATE characters SET vitality_points = ?"))
			{
				st.setInt(1, PcStat.MAX_VITALITY_POINTS);
				st.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Error while updating vitality", e);
		}
		LOGGER.info("Vitality resetted");
	}
	
	private void resetClanBonus()
	{
		ClanTable.getInstance().getClans().forEach(L2Clan::resetClanBonus);
		LOGGER.info("Daily clan bonus has been resetted.");
	}
	
	private void resetExtendDrop()
	{
		// Update data for offline players.
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = ?"))
		{
			ps.setString(1, PlayerVariables.EXTEND_DROP);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Could not reset extend drop : ", e);
		}
		
		// Update data for online players.
		World.getInstance().getPlayers().stream().forEach(player ->
		{
			player.getVariables().set(PlayerVariables.EXTEND_DROP, "");
			player.getVariables().storeMe();
		});
		
		LOGGER.info("Daily world chat points has been resetted.");
	}
	
	private void resetDailySkills()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final List<SkillHolder> dailySkills = getVariables().getList("reset_skills", SkillHolder.class, Collections.emptyList());
			for (SkillHolder skill : dailySkills)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=?;"))
				{
					ps.setInt(1, skill.getSkillId());
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Could not reset daily skill reuse: ", e);
		}
		LOGGER.info("Daily skill reuse cleaned.");
	}
	
	private void resetWorldChatPoints()
	{
		// Update data for offline players.
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val = ? WHERE var = ?"))
		{
			ps.setInt(1, Config.WORLD_CHAT_POINTS_PER_DAY);
			ps.setString(2, PlayerVariables.WORLD_CHAT_VARIABLE_NAME);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Could not reset daily world chat points: ", e);
		}
		
		// Update data for online players.
		World.getInstance().getPlayers().stream().forEach(player ->
		{
			player.setWorldChatPoints(Config.WORLD_CHAT_POINTS_PER_DAY);
			player.sendPacket(new ExWorldChatCnt(player));
			player.getVariables().storeMe();
		});
		
		LOGGER.info("Daily world chat points has been resetted.");
	}
	
	private void resetRecommends()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = 0 WHERE rec_have <= 20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = GREATEST(rec_have - 20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Could not reset Recommendations System: ", e);
		}
		
		World.getInstance().getPlayers().stream().forEach(player ->
		{
			player.setRecomLeft(0);
			player.setRecomHave(player.getRecomHave() - 20);
			player.sendPacket(new ExVoteSystemInfo(player));
			player.broadcastUserInfo();
		});
	}
	
	private void resetOneDayReward()
	{
		OneDayRewardData.getInstance().getOneDayRewardData().forEach(OneDayRewardDataHolder::reset);
	}
	
	public static DailyTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyTaskManager INSTANCE = new DailyTaskManager();
	}
}
