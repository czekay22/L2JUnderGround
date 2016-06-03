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
package quests.Q10335_RequestToFindSakum;

import java.util.HashSet;
import java.util.Set;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.NpcLogListHolder;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Request To Find Sakum (10335)
 * @author St3eT
 */
public final class Q10335_RequestToFindSakum extends Quest
{
	// NPCs
	private static final int BATHIS = 30332;
	private static final int KALLESIN = 33177;
	private static final int ZENATH = 33509;
	private static final int SKELETON_TRACKER = 20035;
	private static final int SKELETON_BOWMAN = 20051;
	private static final int RUIN_SPARTOI = 20054;
	private static final int RUIN_ZOMBIE = 20026;
	private static final int RUIN_ZOMBIE_LEADER = 20029;
	// Misc
	private static final int MIN_LEVEL = 23;
	private static final int MAX_LEVEL = 40;
	
	public Q10335_RequestToFindSakum()
	{
		super(10335);
		addStartNpc(BATHIS);
		addTalkId(BATHIS, KALLESIN, ZENATH);
		addKillId(SKELETON_TRACKER, SKELETON_BOWMAN, RUIN_SPARTOI, RUIN_ZOMBIE, RUIN_ZOMBIE_LEADER);
		addCondNotRace(Race.ERTHEIA, "30332-08.htm");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30332-07.htm");
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
			case "30332-02.htm":
			case "33509-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30332-03.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "33177-02.htm":
			{
				if (st.isCond(1))
				{
					st.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "33509-04.htm":
			{
				if (st.isCond(3))
				{
					giveAdena(player, 900, true);
					addExpAndSp(player, 350000, 84);
					st.exitQuest(false, true);
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
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == BATHIS)
				{
					htmltext = "30332-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = st.isCond(1) ? "30332-04.htm" : "30332-05.htm";
						break;
					}
					case KALLESIN:
					{
						switch (st.getCond())
						{
							case 1:
							{
								htmltext = "33177-01.htm";
								break;
							}
							case 2:
							{
								htmltext = "33177-03.htm";
								break;
							}
							case 3:
							{
								htmltext = "33177-04.htm";
								break;
							}
						}
						break;
					}
					case ZENATH:
					{
						switch (st.getCond())
						{
							case 1:
							case 2:
							{
								htmltext = "33509-01.htm";
								break;
							}
							case 3:
							{
								htmltext = "33509-02.htm";
								break;
							}
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case BATHIS:
					{
						htmltext = "30332-06.htm";
						break;
					}
					case KALLESIN:
					{
						htmltext = "33177-05.htm";
						break;
					}
					case ZENATH:
					{
						htmltext = "33509-05.htm";
						break;
					}
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
		
		if ((st != null) && st.isStarted() && st.isCond(2))
		{
			int killedTracker = st.getInt("killed_" + SKELETON_TRACKER);
			int killedBowman = st.getInt("killed_" + SKELETON_BOWMAN);
			int killedRuinSpartois = st.getInt("killed_" + RUIN_SPARTOI);
			int killedZombie = st.getInt("killed_" + RUIN_ZOMBIE);
			
			switch (npc.getId())
			{
				case SKELETON_TRACKER:
				{
					if (killedTracker < 10)
					{
						killedTracker++;
						st.set("killed_" + SKELETON_TRACKER, killedTracker);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case SKELETON_BOWMAN:
				{
					if (killedBowman < 10)
					{
						killedBowman++;
						st.set("killed_" + SKELETON_BOWMAN, killedBowman);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case RUIN_SPARTOI:
				{
					if (killedRuinSpartois < 15)
					{
						killedRuinSpartois++;
						st.set("killed_" + RUIN_SPARTOI, killedRuinSpartois);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case RUIN_ZOMBIE:
				case RUIN_ZOMBIE_LEADER:
				{
					if (killedZombie < 15)
					{
						killedZombie++;
						st.set("killed_" + RUIN_ZOMBIE, killedZombie);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
			}
			
			if ((killedTracker == 10) && (killedBowman == 10) && (killedRuinSpartois == 15) && (killedZombie == 15))
			{
				st.setCond(3, true);
			}
			sendNpcLogList(killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance activeChar)
	{
		final QuestState st = getQuestState(activeChar, false);
		if ((st != null) && st.isStarted() && st.isCond(2))
		{
			final Set<NpcLogListHolder> npcLogList = new HashSet<>(4);
			npcLogList.add(new NpcLogListHolder(SKELETON_TRACKER, false, st.getInt("killed_" + SKELETON_TRACKER)));
			npcLogList.add(new NpcLogListHolder(SKELETON_BOWMAN, false, st.getInt("killed_" + SKELETON_BOWMAN)));
			npcLogList.add(new NpcLogListHolder(RUIN_SPARTOI, false, st.getInt("killed_" + RUIN_SPARTOI)));
			npcLogList.add(new NpcLogListHolder(RUIN_ZOMBIE, false, st.getInt("killed_" + RUIN_ZOMBIE)));
			return npcLogList;
		}
		return super.getNpcLogList(activeChar);
	}
}