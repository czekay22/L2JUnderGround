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
package org.l2junity.gameserver.model.olympiad;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.instancemanager.InstanceManager;
import org.l2junity.gameserver.model.L2Spawn;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.DoorInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.type.OlympiadStadiumZone;
import org.l2junity.gameserver.network.client.send.ExOlympiadMatchEnd;
import org.l2junity.gameserver.network.client.send.ExOlympiadUserInfo;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JIV
 */
public class OlympiadStadium
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OlympiadStadium.class);
	private final OlympiadStadiumZone _zone;
	private final Instance _instance;
	private final List<L2Spawn> _buffers;
	private OlympiadGameTask _task = null;
	
	protected OlympiadStadium(OlympiadStadiumZone olyzone, int stadium)
	{
		_zone = olyzone;
		_instance = InstanceManager.getInstance().createInstance(olyzone.getInstanceTemplateId(), null);
		_buffers = _instance.getNpcs().stream().map(Npc::getSpawn).collect(Collectors.toList());
		_buffers.stream().map(L2Spawn::getLastSpawn).forEach(Npc::decayMe);
	}
	
	public OlympiadStadiumZone getZone()
	{
		return _zone;
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		_task = task;
	}
	
	public OlympiadGameTask getTask()
	{
		return _task;
	}
	
	public Instance getInstance()
	{
		return _instance;
	}
	
	public final void openDoors()
	{
		_instance.getDoors().forEach(DoorInstance::openMe);
	}
	
	public final void closeDoors()
	{
		_instance.getDoors().forEach(DoorInstance::closeMe);
	}
	
	public final void spawnBuffers()
	{
		_buffers.forEach(L2Spawn::doSpawn);
	}
	
	public final void deleteBuffers()
	{
		_buffers.stream().map(L2Spawn::getLastSpawn).filter(Objects::nonNull).forEach(Npc::deleteMe);
	}
	
	public final void broadcastStatusUpdate(PlayerInstance player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (PlayerInstance target : _instance.getPlayers())
		{
			if (target.inObserverMode() || (target.getOlympiadSide() != player.getOlympiadSide()))
			{
				target.sendPacket(packet);
			}
		}
	}
	
	public final void broadcastPacket(IClientOutgoingPacket packet)
	{
		_instance.broadcastPacket(packet);
	}
	
	public final void broadcastPacketToObservers(IClientOutgoingPacket packet)
	{
		for (PlayerInstance target : _instance.getPlayers())
		{
			if (target.inObserverMode())
			{
				target.sendPacket(packet);
			}
		}
	}
	
	public final void updateZoneStatusForCharactersInside()
	{
		if (_task == null)
		{
			return;
		}
		
		final boolean battleStarted = _task.isBattleStarted();
		final SystemMessage sm;
		if (battleStarted)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
		
		for (PlayerInstance player : _instance.getPlayers())
		{
			if (player.inObserverMode())
			{
				return;
			}
			
			if (battleStarted)
			{
				player.setInsideZone(ZoneId.PVP, true);
				player.sendPacket(sm);
			}
			else
			{
				player.setInsideZone(ZoneId.PVP, false);
				player.sendPacket(sm);
				player.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
			}
		}
	}
	
	public final void updateZoneInfoForObservers()
	{
		if (_task == null)
		{
			return;
		}
		
		for (PlayerInstance player : _instance.getPlayers())
		{
			if (!player.inObserverMode())
			{
				return;
			}
			
			final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
			final List<Location> spectatorSpawns = nextArena.getStadium().getZone().getSpectatorSpawns();
			if (spectatorSpawns.isEmpty())
			{
				LOGGER.warn(getClass().getSimpleName() + ": Zone: " + nextArena.getStadium().getZone() + " doesn't have specatator spawns defined!");
				return;
			}
			final Location loc = spectatorSpawns.get(Rnd.get(spectatorSpawns.size()));
			player.enterOlympiadObserverMode(loc, player.getOlympiadGameId());
		}
	}
}