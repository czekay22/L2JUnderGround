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
package quests.Q00476_PlainMission;

import java.util.HashSet;
import java.util.Set;

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.enums.QuestType;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.NpcLogListHolder;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Plain Mission (476)
 * @author St3eT
 */
public final class Q00476_PlainMission extends Quest
{
	// NPCs
	private static final int ADVENTURER = 32327;
	private static final int ANDREI = 31292;
	private static final int[] GRENDEL =
	{
		21290,
		21291,
		21292,
	};
	private static final int[] BUFFALO =
	{
		21286,
		21287,
		21288,
	};
	private static final int[] ANTELOPE =
	{
		21278,
		21279,
		21280,
	};
	private static final int[] BANDERSNATCH =
	{
		21282,
		21283,
		21284,
	};
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 69;
	
	public Q00476_PlainMission()
	{
		super(476);
		addStartNpc(ADVENTURER);
		addTalkId(ADVENTURER, ANDREI);
		addKillId(BANDERSNATCH);
		addKillId(ANTELOPE);
		addKillId(BUFFALO);
		addKillId(GRENDEL);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "");
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
			case "32327-02.htm":
			case "32327-03.htm":
			{
				htmltext = event;
				break;
			}
			case "32327-04.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player, boolean isSimulated)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == ADVENTURER)
				{
					htmltext = "32327-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(1))
				{
					htmltext = npc.getId() == ADVENTURER ? "32327-05.html" : "31292-03.html";
				}
				else if (st.isCond(2))
				{
					if (npc.getId() == ADVENTURER)
					{
						htmltext = "32327-06.html";
					}
					else if (npc.getId() == ANDREI)
					{
						if (!isSimulated)
						{
							st.exitQuest(QuestType.DAILY, true);
							giveAdena(player, 142_200, true);
							if (player.getLevel() >= MIN_LEVEL)
							{
								addExpAndSp(player, 4_685_175, 1_124);
							}
						}
						htmltext = "31292-01.html";
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if ((npc.getId() == ADVENTURER) && st.isNowAvailable())
				{
					if (!isSimulated)
					{
						st.setState(State.CREATED);
					}
					htmltext = "32327-01.htm";
				}
				else if ((npc.getId() == ANDREI) && st.isCompleted() && !st.isNowAvailable())
				{
					htmltext = "31292-02.html";
				}
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
			int killedAntelope = st.getInt("killed_" + ANTELOPE[0]);
			int killedBandersnatch = st.getInt("killed_" + BANDERSNATCH[0]);
			int killedBuffalo = st.getInt("killed_" + BUFFALO[0]);
			int killedGrendel = st.getInt("killed_" + GRENDEL[0]);
			
			if (CommonUtil.contains(ANTELOPE, npc.getId()))
			{
				if (killedAntelope < 45)
				{
					killedAntelope++;
					st.set("killed_" + ANTELOPE[0], killedAntelope);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (CommonUtil.contains(BANDERSNATCH, npc.getId()))
			{
				if (killedBandersnatch < 45)
				{
					killedBandersnatch++;
					st.set("killed_" + BANDERSNATCH[0], killedBandersnatch);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else if (CommonUtil.contains(BUFFALO, npc.getId()))
			{
				if (killedBuffalo < 45)
				{
					killedBuffalo++;
					st.set("killed_" + BUFFALO[0], killedBuffalo);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			else
			{
				if (killedGrendel < 45)
				{
					killedGrendel++;
					st.set("killed_" + GRENDEL[0], killedGrendel);
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
			
			if ((killedAntelope == 45) && (killedBandersnatch == 45) && (killedBuffalo == 45) && (killedGrendel == 45))
			{
				st.setCond(2, true);
			}
			sendNpcLogList(killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance activeChar)
	{
		final QuestState st = getQuestState(activeChar, false);
		if ((st != null) && st.isStarted() && st.isCond(1))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(4);
			npcLogList.add(new NpcLogListHolder(ANTELOPE[0], false, st.getInt("killed_" + ANTELOPE[0])));
			npcLogList.add(new NpcLogListHolder(BANDERSNATCH[0], false, st.getInt("killed_" + BANDERSNATCH[0])));
			npcLogList.add(new NpcLogListHolder(BUFFALO[0], false, st.getInt("killed_" + BUFFALO[0])));
			npcLogList.add(new NpcLogListHolder(GRENDEL[0], false, st.getInt("killed_" + GRENDEL[0])));
			return npcLogList;
		}
		return super.getNpcLogList(activeChar);
	}
}