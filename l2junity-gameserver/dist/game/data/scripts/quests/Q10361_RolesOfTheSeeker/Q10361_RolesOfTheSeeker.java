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
package quests.Q10361_RolesOfTheSeeker;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * Roles of the Seeker (10361)
 * @author Gladicek
 */
public final class Q10361_RolesOfTheSeeker extends Quest
{
	// NPCs
	private static final int LAKCIS = 32977;
	private static final int CHESHA = 33449;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int MAX_LEVEL = 20;
	
	public Q10361_RolesOfTheSeeker()
	{
		super(10361);
		addStartNpc(LAKCIS);
		addTalkId(LAKCIS, CHESHA);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "32977-05.htm");
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
			case "32977-02.htm":
			case "33449-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32977-03.htm":
			{
				qs.startQuest();
				showOnScreenMsg(player, NpcStringId.ENTER_THE_RUINS_OF_YE_SAGIRA_THROUGH_THE_YE_SAGIRA_TELEPORT_DEVICE, ExShowScreenMessage.TOP_CENTER, 4500);
				htmltext = event;
				break;
			}
			case "33449-03.htm":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 340, true);
					addExpAndSp(player, 35000, 5);
					qs.exitQuest(false, true);
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
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == LAKCIS)
				{
					htmltext = "32977-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-04.htm" : "33449-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == LAKCIS ? "32977-06.htm" : "33449-04.htm";
				break;
			}
		}
		return htmltext;
	}
}