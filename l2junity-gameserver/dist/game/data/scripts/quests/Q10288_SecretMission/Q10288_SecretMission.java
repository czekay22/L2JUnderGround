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
package quests.Q10288_SecretMission;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Secret Mission (10288)
 * @author Gnacik
 */
public class Q10288_SecretMission extends Quest
{
	// NPCs
	private static final int DOMINIC = 31350;
	private static final int AQUILANI = 32780;
	private static final int GREYMORE = 32757;
	// Item
	private static final int LETTER = 15529;
	// Location
	private static final Location TELEPORT = new Location(118833, -80589, -2688);
	
	public Q10288_SecretMission()
	{
		super(10288);
		addStartNpc(AQUILANI, DOMINIC);
		addFirstTalkId(AQUILANI);
		addTalkId(DOMINIC, GREYMORE, AQUILANI);
		registerQuestItems(LETTER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		QuestState st = getQuestState(player, false);
		
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		String htmltext = event;
		
		switch (event)
		{
			case "31350-03.html":
				if (player.getLevel() < 82)
				{
					htmltext = "31350-02b.html";
				}
				break;
			case "31350-05.htm":
				st.startQuest();
				giveItems(player, LETTER, 1);
				break;
			case "32780-03.html":
				if (st.isCond(1) && hasQuestItems(player, LETTER))
				{
					st.setCond(2, true);
				}
				break;
			case "32757-03.html":
				if (st.isCond(2) && hasQuestItems(player, LETTER))
				{
					giveAdena(player, 106583, true);
					addExpAndSp(player, 417788, 46320);
					st.exitQuest(false, true);
				}
				break;
			case "teleport":
				if ((npc.getId() == AQUILANI) && st.isCompleted())
				{
					player.teleToLocation(TELEPORT);
					return null;
				}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		QuestState st = getQuestState(player, false);
		// dialog only changes when you talk to Aquilani after quest completion
		if ((st != null) && st.isCompleted())
		{
			return "32780-05.html";
		}
		return "data/html/default/32780.htm";
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
		
		switch (npc.getId())
		{
			case DOMINIC:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = "31350-01.htm";
						break;
					case State.STARTED:
						if (st.isCond(1))
						{
							htmltext = "31350-06.html";
						}
						break;
					case State.COMPLETED:
						htmltext = "31350-07.html";
						break;
				}
				break;
			case AQUILANI:
				if (st.isStarted())
				{
					if (st.isCond(1) && hasQuestItems(player, LETTER))
					{
						htmltext = "32780-01.html";
					}
					else if (st.isCond(2))
					{
						htmltext = "32780-04.html";
					}
				}
				break;
			case GREYMORE:
				if (st.isStarted() && st.isCond(2) && hasQuestItems(player, LETTER))
				{
					return "32757-01.html";
				}
				break;
		}
		return htmltext;
	}
}
