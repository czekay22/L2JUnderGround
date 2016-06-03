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
package quests.Q10327_IntruderWhoWantsTheBookOfGiants;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import quests.Q10326_RespectYourElders.Q10326_RespectYourElders;

/**
 * Intruder Who Wants the Book of Giants (10327)
 * @author Gladicek
 */
public final class Q10327_IntruderWhoWantsTheBookOfGiants extends Quest
{
	// NPCs
	private static final int PANTHEON = 32972;
	// Items
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Misc
	private static final int MAX_LEVEL = 20;
	private static final int APPRENTICE_EARRING = 112;
	
	public Q10327_IntruderWhoWantsTheBookOfGiants()
	{
		super(10327);
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON);
		registerQuestItems(THE_WAR_OF_GODS_AND_GIANTS);
		addCondMaxLevel(MAX_LEVEL, "32972-09.htm");
		addCondCompletedQuest(Q10326_RespectYourElders.class.getSimpleName(), "32972-09.htm");
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
			case "32972-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32972-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32972-07.htm":
			{
				if (qs.isCond(3))
				{
					showOnScreenMsg(player, NpcStringId.ACCESSORIES_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
					giveAdena(player, 160, true);
					giveItems(player, APPRENTICE_EARRING, 2);
					addExpAndSp(player, 7800, 5);
					qs.exitQuest(false, true);
				}
				break;
			}
		}
		return htmltext;
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
				htmltext = "32972-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "32972-04.htm";
					break;
				}
				else if (qs.isCond(2))
				{
					htmltext = "32972-05.htm";
					break;
				}
				else if (qs.isCond(3))
				{
					htmltext = "32972-06.htm";
					break;
				}
			}
			case State.COMPLETED:
			{
				htmltext = "32972-08.htm";
				break;
			}
		}
		return htmltext;
	}
}