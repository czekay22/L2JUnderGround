/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual.TalkingIsland;

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

import ai.AbstractNpcAI;

/**
 * Ye Segira Combat Soldier AI.
 * @author Sdw
 */
public class YeSegiraCombatSoldier extends AbstractNpcAI
{
	// NPCs
	private static final int[] COMBAT_SOLDIER =
	{
		19152,
		19153,
		19154 // TODO: Part for following player/attacking same target
	};
	private static final int[] MONSTERS =
	{
		22992, // Stalker
		22991, // Crawler
		22996, // Krapher
		22994, // Avian
		22993, // Critter
		23122, // Eyesaroch
		20094, // Orc Marksman
	};
	
	public YeSegiraCombatSoldier()
	{
		addSpawnId(COMBAT_SOLDIER);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (event.equals("LOOK_AROUND") && (npc != null))
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				World.getInstance().forEachVisibleObjectInRange(npc, Npc.class, 500, chars ->
				{
					if (CommonUtil.contains(MONSTERS, chars.getId()))
					{
						addAttackDesire(npc, chars);
						return;
					}
				});
			}
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		getTimers().addRepeatingTimer("LOOK_AROUND", 5000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new YeSegiraCombatSoldier();
	}
}
