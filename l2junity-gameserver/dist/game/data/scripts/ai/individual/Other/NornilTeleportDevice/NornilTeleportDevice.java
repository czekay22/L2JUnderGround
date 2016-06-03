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
package ai.individual.Other.NornilTeleportDevice;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

import ai.AbstractNpcAI;

/**
 * Nornil Teleport Device AI.
 * @author St3eT
 */
public final class NornilTeleportDevice extends AbstractNpcAI
{
	// NPCs
	private static final int DEVICE = 33790; // Nornil Teleport Device
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(-79667, 54028, -4824),
		new Location(-87483, 54024, -4440),
		new Location(-87839, 49803, -4344),
		new Location(-84995, 50974, -4600),
		new Location(-86945, 42814, -2656),
	};
	
	public NornilTeleportDevice()
	{
		addStartNpc(DEVICE);
		addFirstTalkId(DEVICE);
		addTalkId(DEVICE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.startsWith("teleport_"))
		{
			final int locId = Integer.parseInt(event.replace("teleport_", ""));
			player.teleToLocation(LOCATIONS[locId - 1]);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return "NornilTeleportDevice-" + npc.getParameters().getInt("device_place", 0) + ".html";
	}
	
	public static void main(String[] args)
	{
		new NornilTeleportDevice();
	}
}