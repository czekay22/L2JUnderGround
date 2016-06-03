/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10420_TheVarkaSilenosSupporters;

import java.util.HashSet;
import java.util.Set;

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.NpcLogListHolder;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * The Varka Silenos Supporters (10420)
 * @author St3eT
 */
public final class Q10420_TheVarkaSilenosSupporters extends Quest
{
	// NPCs
	private static final int HANSEN = 33853;
	private static final int EMBRYO_SHOOTER = 27514;
	private static final int EMBRYO_WIZARD = 27515;
	private static final int[] SHOOTER_MONSTERS =
	{
		21350, // Varka Silenos Recruit
		21351, // Varka Silenos Footman
		21353, // Varka Silenos Scout
		21354, // Varka Silenos Hunter
		21355, // Varka Silenos Shaman
		21356, // Grazing Nepenthes
		21358, // Varka Silenos Warrior
		21369, // Varka's Commander
		27514, // Varka Backup Shooter
	};
	private static final int[] WIZARD_MONSTERS =
	{
		21357, // Varka Silenos Priest
		27515, // Varka Backup Wizard
	};
	// Items
	private static final int EAS = 26353; // Scroll: Enchant Armor (S-grade)
	// Misc
	private static final int MIN_LEVEL = 76;
	
	public Q10420_TheVarkaSilenosSupporters()
	{
		super(10420);
		addStartNpc(HANSEN);
		addTalkId(HANSEN);
		addKillId(SHOOTER_MONSTERS);
		addKillId(WIZARD_MONSTERS);
		addCondNotRace(Race.ERTHEIA, "33853-09.html");
		addCondInCategory(CategoryType.FIGHTER_GROUP, "33853-08.htm");
		addCondMinLevel(MIN_LEVEL, "33853-08.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33853-02.htm":
			case "33853-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33853-04.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "33853-07.html":
			{
				if (st.isCond(2))
				{
					st.exitQuest(false, true);
					giveItems(player, EAS, 2);
					giveStoryQuestReward(player, 89);
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 22_997_520, 5519);
					}
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = null;
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = "33853-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = st.isCond(1) ? "33853-05.html" : "33853-06.html";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState st = getQuestState(killer, false);
		
		if ((st != null) && st.isCond(1))
		{
			if ((npc.getId() == EMBRYO_SHOOTER) || (npc.getId() == EMBRYO_WIZARD))
			{
				int shooterCount = st.getInt("KillCount_" + EMBRYO_SHOOTER);
				int wizardCount = st.getInt("KillCount_" + EMBRYO_WIZARD);
				
				if (npc.getId() == EMBRYO_SHOOTER)
				{
					if (shooterCount < 50)
					{
						st.set("KillCount_" + EMBRYO_SHOOTER, ++shooterCount);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else
				{
					if (wizardCount < 50)
					{
						st.set("KillCount_" + EMBRYO_WIZARD, ++wizardCount);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				
				if ((shooterCount >= 50) && (wizardCount >= 50))
				{
					st.setCond(2, true);
				}
			}
			else
			{
				if (CommonUtil.contains(WIZARD_MONSTERS, npc.getId()))
				{
					if (st.getInt("KillCount_" + EMBRYO_WIZARD) < 50)
					{
						final Npc embryo = addSpawn(EMBRYO_WIZARD, npc, false, 60000);
						addAttackPlayerDesire(embryo, killer);
						embryo.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_DARE_INTERFERE_WITH_EMBRYO_SURELY_YOU_WISH_FOR_DEATH);
					}
				}
				else
				{
					if (st.getInt("KillCount_" + EMBRYO_SHOOTER) < 50)
					{
						final Npc embryo = addSpawn(EMBRYO_SHOOTER, npc, false, 60000);
						addAttackPlayerDesire(embryo, killer);
						embryo.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_DARE_INTERFERE_WITH_EMBRYO_SURELY_YOU_WISH_FOR_DEATH);
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance activeChar)
	{
		final QuestState st = getQuestState(activeChar, false);
		if ((st != null) && st.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(2);
			npcLogList.add(new NpcLogListHolder(EMBRYO_SHOOTER, false, st.getInt("KillCount_" + EMBRYO_SHOOTER)));
			npcLogList.add(new NpcLogListHolder(EMBRYO_WIZARD, false, st.getInt("KillCount_" + EMBRYO_WIZARD)));
			return npcLogList;
		}
		return super.getNpcLogList(activeChar);
	}
}