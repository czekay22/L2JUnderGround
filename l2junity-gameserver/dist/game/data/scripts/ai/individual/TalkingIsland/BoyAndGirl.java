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

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;
import org.l2junity.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Boy and Girl AI.
 * @author St3eT
 */
public final class BoyAndGirl extends AbstractNpcAI
{
	// NPCs
	private static final int BOY = 33224;
	private static final int GIRL = 33217;
	// Items
	private static final int WEAPON = 15304;
	
	private BoyAndGirl()
	{
		addSpawnId(BOY, GIRL);
		addMoveFinishedId(BOY, GIRL);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (event.equals("NPC_CHANGEWEAP"))
		{
			if (npc.isScriptValue(1))
			{
				npc.setRHandId(0);
				npc.setScriptValue(0);
			}
			else
			{
				npc.setRHandId(WEAPON);
				npc.setScriptValue(1);
			}
			getTimers().addTimer("NPC_CHANGEWEAP", 15000 + (getRandom(5) * 1000), npc, null);
		}
		else if (event.equals("NPC_SHOUT"))
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, npc.getId() == BOY ? NpcStringId.WEEE : NpcStringId.BOYS_ARE_SO_ANNOYING);
			getTimers().addTimer("NPC_SHOUT", 10000 + (getRandom(5) * 1000), npc, null);
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		getTimers().addTimer("NPC_CHANGEWEAP", 15000 + (getRandom(5) * 1000), npc, null);
		getTimers().addTimer("NPC_SHOUT", 10000 + (getRandom(5) * 1000), npc, null);
		npc.setIsRunning(true);
		addMoveToDesire(npc, GeoData.getInstance().moveCheck(npc.getLocation(), Util.getRandomPosition(npc.getSpawn().getLocation(), 200, 600), npc.getInstanceWorld()), 23);
		return super.onSpawn(npc);
	}
	
	@Override
	public void onMoveFinished(Npc npc)
	{
		addMoveToDesire(npc, GeoData.getInstance().moveCheck(npc.getLocation(), Util.getRandomPosition(npc.getSpawn().getLocation(), 200, 600), npc.getInstanceWorld()), 23);
		super.onMoveFinished(npc);
	}
	
	public static void main(String[] args)
	{
		new BoyAndGirl();
	}
}