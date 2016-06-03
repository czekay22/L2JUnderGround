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
package ai.individual.TalkingIsland.Pantheon;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import ai.AbstractNpcAI;
import quests.Q10320_LetsGoToTheCentralSquare.Q10320_LetsGoToTheCentralSquare;

/**
 * Pantheon AI.
 * @author Gladicek
 */
public final class Pantheon extends AbstractNpcAI
{
	// NPC
	private static final int PANTHEON = 32972;
	// Location
	private static final Location MUSEUM = new Location(-114711, 243911, -7968);
	// Misc
	private static final int MIN_LEVEL = 20;
	
	private Pantheon()
	{
		addSpawnId(PANTHEON);
		addStartNpc(PANTHEON);
		addFirstTalkId(PANTHEON);
		addTalkId(PANTHEON);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "32972-1.html":
			{
				htmltext = event;
				break;
			}
			case "teleport_museum":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "32972-noteleport.html";
				}
				else
				{
					player.teleToLocation(MUSEUM);
				}
				break;
			}
			case "TEXT_SPAM":
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.IS_IT_BETTER_TO_END_DESTINY_OR_START_DESTINY);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(Q10320_LetsGoToTheCentralSquare.class.getSimpleName());
		if (st == null)
		{
			showOnScreenMsg(player, NpcStringId.BEGIN_TUTORIAL_QUESTS, ExShowScreenMessage.TOP_CENTER, 4500);
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("TEXT_SPAM", 10000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Pantheon();
	}
}