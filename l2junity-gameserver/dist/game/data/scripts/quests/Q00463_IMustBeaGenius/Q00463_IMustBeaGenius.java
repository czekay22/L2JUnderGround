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
package quests.Q00463_IMustBeaGenius;

import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.enums.QuestType;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * I Must Be a Genius (463)<br>
 * @author Gnacik, malyelfik
 * @version 2010-08-19 Based on Freya PTS
 */
public class Q00463_IMustBeaGenius extends Quest
{
	private static class DropInfo
	{
		private final int _count;
		private final int _chance;
		
		public DropInfo(int count, int chance)
		{
			_count = count;
			_chance = chance;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public int getSpecialChance()
		{
			return _chance;
		}
	}
	
	// NPC
	private static final int GUTENHAGEN = 32069;
	// Items
	private static final int CORPSE_LOG = 15510;
	private static final int COLLECTION = 15511;
	
	// Mobs
	private static final Map<Integer, DropInfo> MOBS = new HashMap<>();
	
	static
	{
		MOBS.put(22801, new DropInfo(5, 0));
		MOBS.put(22802, new DropInfo(5, 0));
		MOBS.put(22803, new DropInfo(5, 0));
		MOBS.put(22804, new DropInfo(-2, 1));
		MOBS.put(22805, new DropInfo(-2, 1));
		MOBS.put(22806, new DropInfo(-2, 1));
		MOBS.put(22807, new DropInfo(-1, -1));
		MOBS.put(22809, new DropInfo(2, 2));
		MOBS.put(22810, new DropInfo(-3, 3));
		MOBS.put(22811, new DropInfo(3, -1));
		MOBS.put(22812, new DropInfo(1, -1));
	}
	
	// Reward @formatter:off
	private static final int[][] REWARD =
	{
		// exp, sp, html
		{198725, 15892, 8},
		{278216, 22249, 8},
		{317961, 25427, 8},
		{357706, 28606, 9},
		{397451, 31784, 9},
		{596176, 47677, 9},
		{715411, 57212, 10},
		{794901, 63569, 10},
		{914137, 73104, 10},
		{1192352, 95353, 11}
	};
	
	// Misc @formatter:on
	private static final int MIN_LEVEL = 70;
	
	public Q00463_IMustBeaGenius()
	{
		super(463);
		addStartNpc(GUTENHAGEN);
		addTalkId(GUTENHAGEN);
		addKillId(MOBS.keySet());
		registerQuestItems(COLLECTION, CORPSE_LOG);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "32069-03.htm":
				st.startQuest();
				int number = getRandom(51) + 550;
				st.set("number", String.valueOf(number));
				st.set("chance", String.valueOf(getRandom(4)));
				htmltext = getHtm(player.getHtmlPrefix(), event).replace("%num%", String.valueOf(number));
				break;
			case "32069-05.htm":
				htmltext = getHtm(player.getHtmlPrefix(), event).replace("%num%", st.get("number"));
				break;
			case "reward":
				if (st.isCond(2))
				{
					final int rnd = getRandom(REWARD.length);
					final String str = (REWARD[rnd][2] < 10) ? "0" + REWARD[rnd][2] : String.valueOf(REWARD[rnd][2]);
					
					addExpAndSp(player, REWARD[rnd][0], REWARD[rnd][1]);
					st.exitQuest(QuestType.DAILY, true);
					htmltext = "32069-" + str + ".html";
				}
				break;
			case "32069-02.htm":
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		if (st.isCond(1))
		{
			boolean msg = false;
			int number = MOBS.get(npc.getId()).getCount();
			
			if (MOBS.get(npc.getId()).getSpecialChance() == st.getInt("chance"))
			{
				number = getRandom(100) + 1;
			}
			
			if (number > 0)
			{
				giveItems(player, CORPSE_LOG, number);
				msg = true;
			}
			else if ((number < 0) && ((getQuestItemsCount(player, CORPSE_LOG) + number) > 0))
			{
				takeItems(player, CORPSE_LOG, Math.abs(number));
				msg = true;
			}
			
			if (msg)
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ATT_ATTACK_S1_RO_ROGUE_S2, player.getName(), String.valueOf(number));
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if (getQuestItemsCount(player, CORPSE_LOG) == st.getInt("number"))
				{
					takeItems(player, CORPSE_LOG, -1);
					giveItems(player, COLLECTION, 1);
					st.setCond(2, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.COMPLETED:
				if (!st.isNowAvailable())
				{
					htmltext = "32069-07.htm";
					break;
				}
				st.setState(State.CREATED);
			case State.CREATED:
				htmltext = (player.getLevel() >= MIN_LEVEL) ? "32069-01.htm" : "32069-00.htm";
				break;
			case State.STARTED:
				if (st.isCond(1))
				{
					htmltext = "32069-04.html";
				}
				else
				{
					if (st.getInt("var") == 1)
					{
						htmltext = "32069-06a.html";
					}
					else
					{
						takeItems(player, COLLECTION, -1);
						st.set("var", "1");
						htmltext = "32069-06.html";
					}
				}
				break;
		}
		return htmltext;
	}
}