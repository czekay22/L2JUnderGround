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
package quests.Q10334_ReportingTheStatusOfTheWindmillHill;

import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

import quests.Q10333_DisappearedSakum.Q10333_DisappearedSakum;

/**
 * Reporting The Status Of The Windmill Hill (10334)
 * @author St3eT
 */
public final class Q10334_ReportingTheStatusOfTheWindmillHill extends Quest
{
	// NPCs
	private static final int SCHUNAIN = 33508;
	private static final int BATHIS = 30332;
	// Misc
	private static final int MIN_LEVEL = 22;
	private static final int MAX_LEVEL = 40;
	
	public Q10334_ReportingTheStatusOfTheWindmillHill()
	{
		super(10334);
		addStartNpc(SCHUNAIN);
		addTalkId(SCHUNAIN, BATHIS);
		addCondNotRace(Race.ERTHEIA, "33508-07.htm");
		addCondCompletedQuest(Q10333_DisappearedSakum.class.getSimpleName(), "33508-06.htm");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33508-06.htm");
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
			case "33508-02.htm":
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33508-03.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30332-03.htm":
			{
				if (st.isCond(1))
				{
					giveAdena(player, 850, true);
					addExpAndSp(player, 200000, 48);
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
				if (npc.getId() == SCHUNAIN)
				{
					htmltext = "33508-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == SCHUNAIN ? "33508-04.htm" : "30332-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == SCHUNAIN ? "33508-05.htm" : "30332-04.htm";
				break;
			}
		}
		return htmltext;
	}
}