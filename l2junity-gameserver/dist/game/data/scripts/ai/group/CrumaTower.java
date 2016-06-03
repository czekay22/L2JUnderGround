/*
 * Copyright (C) 2004-2015 L2J Unity
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
package ai.group;

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.Id;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureDamageReceived;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Cruma Tower AI
 * @author malyelfik
 */
public final class CrumaTower extends AbstractNpcAI
{
	// NPCs
	private static final int CARSUS = 30483;
	private static final int TELEPORT_DEVICE = 33157;
	
	public CrumaTower()
	{
		addSpawnId(CARSUS);
		addAttackId(TELEPORT_DEVICE);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (event.equals("MESSAGE") && (npc != null))
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_CAN_GO_TO_UNDERGROUND_LV_3_USING_THE_ELEVATOR_IN_THE_BACK);
			getTimers().addTimer(event, 15000, npc, player);
		}
		else
		{
			super.onTimerEvent(event, params, npc, player);
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		getTimers().addTimer("MESSAGE", 15000, npc, null);
		return super.onSpawn(npc);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DAMAGE_RECEIVED)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(TELEPORT_DEVICE)
	public void onCreatureDamageReceived(OnCreatureDamageReceived event)
	{
		try
		{
			final Npc npc = (Npc) event.getTarget();
			final int[] location = npc.getParameters().getIntArray("teleport", ";");
			event.getAttacker().teleToLocation(location[0], location[1], location[2]);
		}
		catch (Exception e)
		{
			_log.warn("Invalid location for Cruma Tower teleport device.");
		}
	}
	
	public static void main(String[] args)
	{
		new CrumaTower();
	}
}