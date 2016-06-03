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
package quests.Q10364_ObligationsOfTheSeeker;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import quests.Q10363_RequestOfTheSeeker.Q10363_RequestOfTheSeeker;

/**
 * Obligations of the Seeker (10364)
 * @author Gladicek
 */
public final class Q10364_ObligationsOfTheSeeker extends Quest
{
	// NPCs
	private static final int CELIN = 33451;
	private static final int WALTER = 33452;
	private static final int DEP = 33453;
	private static final int KRAPHER = 22996;
	private static final int AVIAN = 22994;
	// Items
	private static final int DIRTY_PAPER_PIECES = 17578;
	private static final int LEATHER_SHOES = 37;
	private static final int HEALING_POTION = 1060;
	// Misc
	private static final int MIN_LEVEL = 14;
	private static final int MAX_LEVEL = 25;
	
	public Q10364_ObligationsOfTheSeeker()
	{
		super(10364);
		addStartNpc(CELIN);
		addTalkId(CELIN, WALTER, DEP);
		addKillId(KRAPHER, AVIAN);
		registerQuestItems(DIRTY_PAPER_PIECES);
		addCondMinLevel(MIN_LEVEL, "33451-04.htm");
		addCondMaxLevel(MAX_LEVEL, "33451-04.htm");
		addCondCompletedQuest(Q10363_RequestOfTheSeeker.class.getSimpleName(), "33451-04.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33451-02.htm":
			case "33452-02.htm":
			case "33452-03.htm":
			case "33453-02.htm":
			case "33453-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33451-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33452-04.htm":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
					htmltext = event;
					break;
				}
				break;
			}
			case "33453-04.htm":
			{
				if (qs.isCond(3))
				{
					giveItems(player, LEATHER_SHOES, 1);
					giveAdena(player, 550, true);
					giveItems(player, HEALING_POTION, 50);
					addExpAndSp(player, 95000, 22);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(2))
		{
			if (giveItemRandomly(killer, npc, DIRTY_PAPER_PIECES, 1, 5, 0.5, true))
			{
				qs.setCond(3);
				showOnScreenMsg(killer, NpcStringId.USE_THE_YE_SAGIRA_TELEPORT_DEVICE_TO_GO_TO_EXPLORATION_AREA_4, ExShowScreenMessage.TOP_CENTER, 4500);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == CELIN)
				{
					htmltext = "33451-01.htm";
					break;
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case CELIN:
					{
						if (qs.isCond(1))
						{
							htmltext = "33451-03.htm";
							break;
						}
						break;
					}
					case WALTER:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "33452-01.htm";
								break;
							}
							case 2:
							{
								htmltext = "33452-05.htm";
								break;
							}
							case 3:
							{
								htmltext = "33452-06.htm";
								break;
							}
						}
						break;
					}
					case DEP:
					{
						if (qs.isCond(3))
						{
							htmltext = "33453-01.htm";
							break;
						}
					}
						break;
				}
				break;
			}
			case State.COMPLETED:
			{
				switch (npc.getId())
				{
					case CELIN:
					{
						htmltext = "33451-05.htm";
						break;
					}
					case WALTER:
					{
						htmltext = "33452-07.htm";
						break;
					}
					case DEP:
					{
						htmltext = "33453-05.htm";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
}