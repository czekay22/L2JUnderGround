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
package quests.Q10732_AForeignLand;

import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.network.client.send.ExShowUsm;
import org.l2junity.gameserver.network.client.send.TutorialShowHtml;

/**
 * A Foreign Land (10732)
 * @author Sdw
 */
public final class Q10732_AForeignLand extends Quest
{
	// NPC's
	private static final int NAVARI = 33931;
	private static final int GERETH = 33932;
	// Misc
	private static final int MAX_LEVEL = 20;
	
	public Q10732_AForeignLand()
	{
		super(10732);
		addStartNpc(NAVARI);
		addTalkId(NAVARI, GERETH);
		addCondRace(Race.ERTHEIA, "");
		addCondMaxLevel(MAX_LEVEL, "33931-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "33931-02.htm":
				break;
			case "33931-03.htm":
			{
				qs.startQuest();
				player.sendPacket(ExShowUsm.ERTHEIA_FIRST_QUEST);
				break;
			}
			case "33932-02.html":
			{
				if (qs.isStarted())
				{
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_001_Radar_01.htm", TutorialShowHtml.LARGE_WINDOW));
					giveAdena(player, 3000, true);
					addExpAndSp(player, 75, 2);
					qs.exitQuest(false, true);
				}
				break;
			}
			default:
				htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, true);
		if (st.isCompleted())
		{
			return getAlreadyCompletedMsg(player);
		}
		
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case NAVARI:
			{
				if (st.isCreated())
				{
					htmltext = "33931-01.htm";
				}
				else if (st.isStarted())
				{
					htmltext = "33931-04.html";
				}
				break;
			}
			case GERETH:
			{
				if (st.isStarted())
				{
					htmltext = "33932-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
}