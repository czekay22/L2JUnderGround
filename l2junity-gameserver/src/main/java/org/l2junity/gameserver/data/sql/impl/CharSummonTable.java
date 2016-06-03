/*
 * Copyright (C) 2004-2015 L2J Unity
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
package org.l2junity.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.data.xml.impl.NpcData;
import org.l2junity.gameserver.data.xml.impl.PetDataTable;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.model.PetData;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.L2ServitorInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.PetItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nyaran
 */
public class CharSummonTable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CharSummonTable.class);
	private static final Map<Integer, Integer> _pets = new ConcurrentHashMap<>();
	private static final Map<Integer, Set<Integer>> _servitors = new ConcurrentHashMap<>();
	
	// SQL
	private static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
	private static final String INIT_SUMMONS = "SELECT ownerId, summonId FROM character_summons";
	private static final String LOAD_SUMMON = "SELECT summonSkillId, summonId, curHp, curMp, time FROM character_summons WHERE ownerId = ?";
	private static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ? and summonId = ?";
	private static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?,?)";
	
	public Map<Integer, Integer> getPets()
	{
		return _pets;
	}
	
	public Map<Integer, Set<Integer>> getServitors()
	{
		return _servitors;
	}
	
	public void init()
	{
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_SUMMONS))
			{
				while (rs.next())
				{
					_servitors.computeIfAbsent(rs.getInt("ownerId"), k -> ConcurrentHashMap.newKeySet()).add(rs.getInt("summonId"));
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Error while loading saved servitor: " + e);
			}
		}
		
		if (Config.RESTORE_PET_ON_RECONNECT)
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_PET))
			{
				while (rs.next())
				{
					_pets.put(rs.getInt("ownerId"), rs.getInt("item_obj_id"));
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Error while loading saved pet: " + e);
			}
		}
	}
	
	public void removeServitor(PlayerInstance activeChar, int summonObjectId)
	{
		_servitors.computeIfPresent(activeChar.getObjectId(), (k, v) ->
		{
			v.remove(summonObjectId);
			return !v.isEmpty() ? v : null;
		});
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(REMOVE_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.setInt(2, summonObjectId);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warn("Summon cannot be removed: " + e);
		}
	}
	
	public void restorePet(PlayerInstance activeChar)
	{
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_pets.get(activeChar.getObjectId()));
		if (item == null)
		{
			LOGGER.warn("Null pet summoning item for: " + activeChar);
			return;
		}
		final PetData petData = PetDataTable.getInstance().getPetDataByItemId(item.getId());
		if (petData == null)
		{
			LOGGER.warn("Null pet data for: " + activeChar + " and summoning item: " + item);
			return;
		}
		final L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(petData.getNpcId());
		if (npcTemplate == null)
		{
			LOGGER.warn("Null pet NPC template for: " + activeChar + " and pet Id:" + petData.getNpcId());
			return;
		}
		
		final L2PetInstance pet = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
		if (pet == null)
		{
			LOGGER.warn("Null pet instance for: " + activeChar + " and pet NPC template:" + npcTemplate);
			return;
		}
		
		pet.setShowSummonAnimation(true);
		pet.setTitle(activeChar.getName());
		
		if (!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp());
			pet.setCurrentMp(pet.getMaxMp());
			pet.getStat().setExp(pet.getExpForThisLevel());
			pet.setCurrentFed(pet.getMaxFed());
		}
		
		pet.setRunning();
		
		if (!pet.isRespawned())
		{
			pet.storeMe();
		}
		
		item.setEnchantLevel(pet.getLevel());
		activeChar.setPet(pet);
		pet.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
		pet.startFeed();
		pet.setFollowStatus(true);
		pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
		pet.broadcastStatusUpdate();
	}
	
	public void restoreServitor(PlayerInstance activeChar)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				Skill skill;
				while (rs.next())
				{
					int summonObjId = rs.getInt("summonId");
					int skillId = rs.getInt("summonSkillId");
					int curHp = rs.getInt("curHp");
					int curMp = rs.getInt("curMp");
					int time = rs.getInt("time");
					
					skill = SkillData.getInstance().getSkill(skillId, activeChar.getSkillLevel(skillId));
					if ((skill == null) || !activeChar.hasServitor(summonObjId))
					{
						removeServitor(activeChar, summonObjId);
						return;
					}
					
					skill.applyEffects(activeChar, activeChar);
					
					final L2ServitorInstance summon = (L2ServitorInstance) activeChar.getServitor(summonObjId);
					summon.setCurrentHp(curHp);
					summon.setCurrentMp(curMp);
					summon.setLifeTimeRemaining(time);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Servitor cannot be restored: " + e);
		}
	}
	
	public void saveSummon(L2ServitorInstance summon)
	{
		if ((summon == null) || (summon.getLifeTimeRemaining() <= 0))
		{
			return;
		}
		
		_servitors.computeIfAbsent(summon.getOwner().getObjectId(), k -> ConcurrentHashMap.newKeySet()).add(summon.getObjectId());
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_SUMMON))
		{
			ps.setInt(1, summon.getOwner().getObjectId());
			ps.setInt(2, summon.getObjectId());
			ps.setInt(3, summon.getReferenceSkill());
			ps.setInt(4, (int) Math.round(summon.getCurrentHp()));
			ps.setInt(5, (int) Math.round(summon.getCurrentMp()));
			ps.setInt(6, summon.getLifeTimeRemaining());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to store summon: {} from {}, error: {}", summon, summon.getOwner(), e);
		}
	}
	
	public static CharSummonTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharSummonTable _instance = new CharSummonTable();
	}
}
