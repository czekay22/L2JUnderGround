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
 * Heymond AI.
 * @author St3eT
 */
public final class Heymond extends AbstractNpcAI
{
	// NPCs
	private static final int BANETTE = 33114;
	
	private Heymond()
	{
		addSpawnId(BANETTE);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		switch (getRandom(4))
		{
			case 0:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.VIEW_OUR_WIDE_VARIETY_OF_ACCESSORIES);
				break;
			case 1:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.THE_BEST_WEAPON_DOESN_T_MAKE_YOU_THE_BEST);
				break;
			case 2:
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WE_BUY_AND_SELL_COME_TAKE_A_LOOK);
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
		new Heymond();
	}
}