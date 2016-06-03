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
package ai.individual.TalkingIsland;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Oris AI.
 * @author Gladicek
 */
public final class Oris extends AbstractNpcAI
{
	// NPCs
	private static final int ORIS = 33116;
	
	private Oris()
	{
		addSpawnId(ORIS);
		addStartNpc(ORIS);
		addTalkId(ORIS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("SOCIAL_ACTION_1"))
		{
			npc.broadcastSocialAction(6);
			startQuestTimer("SOCIAL_ACTION_2", 2500, npc, null);
		}
		else if (event.equals("SOCIAL_ACTION_2"))
		{
			npc.broadcastSocialAction(7);
		}
		else if (event.equals("SPAM_TEXT") && (npc != null))
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.I_HAVEN_T_FELT_THIS_GOOD_IN_AGES, 1000);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setRandomAnimation(false);
		startQuestTimer("SOCIAL_ACTION_1", 6500, npc, null, true);
		startQuestTimer("SPAM_TEXT", 10000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Oris();
	}
}