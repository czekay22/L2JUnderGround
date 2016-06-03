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
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Banette AI.
 * @author St3eT
 */
public final class Banette extends AbstractNpcAI
{
	// NPCs
	private static final int BANETTE = 33114;
	
	private Banette()
	{
		addSpawnId(BANETTE);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		switch (getRandom(4))
		{
			case 0:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.TRAINING_GROUND_IS_LOCATED_STRAIGHT_AHEAD);
				break;
			case 1:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WHILE_TRAINING_IN_THE_TRAINING_GROUNDS_IT_BECOMES_PROGRESSIVELY_DIFFICULT);
				break;
			case 2:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.TRAINING_GROUNDS_ACCESS_YOU_NEED_TO_SPEAK_WITH_PANTHEON_IN_THE_MUSEUM);
				break;
		}
		getTimers().addTimer("NPC_SHOUT", (10 + getRandom(5)) * 1000, npc, null);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		getTimers().addTimer("NPC_SHOUT", (10 + getRandom(5)) * 1000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Banette();
	}
}