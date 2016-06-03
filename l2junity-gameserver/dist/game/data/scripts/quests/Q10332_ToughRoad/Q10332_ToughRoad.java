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
package quests.Q10332_ToughRoad;

import org.l2junity.gameserver.enums.Movie;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import quests.Q10331_StartOfFate.Q10331_StartOfFate;

/**
 * Tough Road (10332)
 * @author St3eT
 */
public final class Q10332_ToughRoad extends Quest
{
	// NPCs
	private static final int KAKAI = 30565;
	private static final int BATHIS = 30332;
	// Misc
	private static final int MIN_LEVEL = 20;
	private static final int MAX_LEVEL = 40;
	private static final int ZONE_ID = 12016;
	private static final String MOVIE_VAR = "Q10332_MOVIE";
	
	public Q10332_ToughRoad()
	{
		super(10332);
		addStartNpc(KAKAI);
		addTalkId(KAKAI, BATHIS);
		addEnterZoneId(ZONE_ID);
		addCondNotRace(Race.ERTHEIA, "30565-05.htm");
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "30565-04.htm");
		addCondCompletedQuest(Q10331_StartOfFate.class.getSimpleName(), "30565-04.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		
		if (st == null)
		{
			if (event.equals("SCREEN_MSG"))
			{
				showOnScreenMsg(player, NpcStringId.PA_AGRIO_LORD_KAKAI_IS_CALLING_FOR_YOU, ExShowScreenMessage.TOP_CENTER, 10000);
			}
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30332-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30565-02.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30332-03.htm":
			{
				if (st.isCond(1))
				{
					giveAdena(player, 700, true);
					addExpAndSp(player, 90000, 21);
					st.exitQuest(false, true);
					player.getVariables().remove(MOVIE_VAR);
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		if (npc.getId() == KAKAI)
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = "30565-01.htm";
					break;
				}
				case State.STARTED:
				{
					htmltext = "30565-06.htm";
					break;
				}
				case State.COMPLETED:
				{
					htmltext = "30565-03.htm";
					break;
				}
			}
		}
		else if (npc.getId() == BATHIS)
		{
			if (st.getState() == State.STARTED)
			{
				htmltext = "30332-01.htm";
			}
			else if (st.getState() == State.COMPLETED)
			{
				htmltext = "30332-04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onEnterZone(Creature character, ZoneType zone)
	{
		if (character.isPlayer())
		{
			final PlayerInstance player = character.getActingPlayer();
			final QuestState st = getQuestState(player, false);
			final QuestState st10331 = player.getQuestState(Q10331_StartOfFate.class.getSimpleName());
			
			if (((st == null) || st.isCreated()) && (player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (st10331 != null) && st10331.isCompleted() && !player.getVariables().getBoolean(MOVIE_VAR, false))
			{
				player.getVariables().set(MOVIE_VAR, true);
				playMovie(player, Movie.SI_ILLUSION_04_QUE);
				startQuestTimer("SCREEN_MSG", 11000, null, player);
			}
		}
		return super.onEnterZone(character, zone);
	}
}