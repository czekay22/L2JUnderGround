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
package ai.individual.FairySettlement;

import org.l2junity.gameserver.model.L2Spawn;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureSee;
import org.l2junity.gameserver.model.holders.SkillHolder;

import ai.AbstractNpcAI;

/**
 * Wisp AI.
 * @author St3eT
 */
public final class Wisp extends AbstractNpcAI
{
	// NPCs
	private static final int WISP = 32915;
	private static final int LARGE_WISP = 32916;
	// Skills
	private static final SkillHolder WISP_HEAL = new SkillHolder(14064, 1);
	private static final SkillHolder LARGE_WISP_HEAL = new SkillHolder(14065, 1);
	// Misc
	private static final int RESPAWN_MIN = 60000;
	private static final int RESPAWN_MAX = 120000;
	
	private Wisp()
	{
		addSpawnId(WISP, LARGE_WISP);
		setCreatureSeeId(this::onCreatureSee, WISP, LARGE_WISP);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (event.equals("DELETE_NPC"))
		{
			params = new StatsSet();
			params.set("LOCATION_OBJECT", npc.getLocation());
			getTimers().addTimer("RESPAWN_WISP_" + npc.getObjectId(), params, getRandom(RESPAWN_MIN, RESPAWN_MAX), null, null);
			npc.deleteMe();
		}
		else
		{
			addSpawn(WISP, params.getObject("LOCATION_OBJECT", Location.class));
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final L2Spawn spawn = npc.getSpawn();
		spawn.stopRespawn();
		
		if ((npc.getId() == WISP) && (getRandom(100) < 10))
		{
			addSpawn(LARGE_WISP, npc);
			npc.deleteMe();
		}
		else
		{
			npc.initSeenCreatures();
		}
		return super.onSpawn(npc);
	}
	
	public void onCreatureSee(OnCreatureSee event)
	{
		final Creature creature = event.getSeen();
		final Npc npc = (Npc) event.getSeer();
		
		if (creature.isPlayer())
		{
			npc.setTarget(creature);
			npc.doCast(npc.getId() == WISP ? WISP_HEAL.getSkill() : LARGE_WISP_HEAL.getSkill());
			getTimers().addTimer("DELETE_NPC", 5000, npc, null);
		}
	}
	
	public static void main(String[] args)
	{
		new Wisp();
	}
}