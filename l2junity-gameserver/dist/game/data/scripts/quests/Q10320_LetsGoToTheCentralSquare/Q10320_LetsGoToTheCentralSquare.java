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
package quests.Q10320_LetsGoToTheCentralSquare;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.enums.Movie;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerCreate;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.network.client.send.TutorialShowHtml;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * Let's Go To The Central Square (10320)
 * @author ivantotov, Gladicek
 */
public final class Q10320_LetsGoToTheCentralSquare extends Quest
{
	// NPCs
	private static final int PANTHEON = 32972;
	private static final int THEODORE = 32975;
	// Zone
	private static final int TALKING_ISLAND_PRESENTATION_MOVIE_ZONE = 200034;
	// Misc
	private static final int MAX_LEVEL = 20;
	private static final String MOVIE_VAR = "TI_presentation_movie";
	
	public Q10320_LetsGoToTheCentralSquare()
	{
		super(10320);
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON, THEODORE);
		addEnterZoneId(TALKING_ISLAND_PRESENTATION_MOVIE_ZONE);
		addCondMaxLevel(MAX_LEVEL, "32972-01a.htm");
		addCondNotRace(Race.ERTHEIA, "32972-01b.htm");
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
			case "32972-03.htm":
			{
				qs.startQuest();
				player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_001_Radar_01.htm", TutorialShowHtml.LARGE_WINDOW));
				htmltext = event;
				break;
			}
			case "32972-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32975-02.htm":
			{
				if (qs.isStarted())
				{
					giveAdena(player, 30, true);
					addExpAndSp(player, 30, 5);
					qs.exitQuest(false, true);
					htmltext = event;
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WAIT_WAIT_A_MINUTE_I_STILL_HAVE_TIME);
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
				htmltext = npc.getId() == PANTHEON ? "32972-01.htm" : "32975-04.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-04.htm" : "32975-01.htm";
				break;
			}
			case State.COMPLETED:
			{
				htmltext = npc.getId() == PANTHEON ? "32972-05.htm" : "32975-03.htm";
				break;
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
			
			if (player.getVariables().getBoolean(MOVIE_VAR, false))
			{
				if (player.getLevel() <= MAX_LEVEL)
				{
					final QuestState qs = getQuestState(player, false);
					playMovie(player, ((qs != null) && qs.isStarted()) ? Movie.SI_ILLUSION_02_QUE : Movie.SI_ILLUSION_01_QUE);
				}
				player.getVariables().remove(MOVIE_VAR);
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CREATE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerCreate(OnPlayerCreate event)
	{
		final PlayerInstance player = event.getActiveChar();
		if (player.getRace() != Race.ERTHEIA)
		{
			player.getVariables().set(MOVIE_VAR, true);
		}
	}
}