/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package org.l2junity.plugins.yal2logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.Containers;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenersContainer;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerLogout;
import org.l2junity.gameserver.model.events.impl.server.OnPacketReceived;
import org.l2junity.gameserver.model.events.impl.server.OnPacketSent;
import org.l2junity.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2junity.gameserver.network.client.IncomingPackets;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.plugins.yal2logger.LogWriters.IPacketHandler;
import org.l2junity.plugins.yal2logger.LogWriters.YAL2Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public final class Yal2LoggerManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Yal2LoggerManager.class);
	private final Map<Integer, IPacketHandler> _logs = new ConcurrentHashMap<>();
	
	protected Yal2LoggerManager()
	{
	
	}
	
	public void init()
	{
		final ListenersContainer container = Containers.Global();
		container.addListener(new ConsumerEventListener(container, EventType.ON_PACKET_RECEIVED, (OnPacketReceived event) -> onPacketReceived(event), this));
		container.addListener(new ConsumerEventListener(container, EventType.ON_PACKET_SENT, (OnPacketSent event) -> onPacketSent(event), this));
		container.addListener(new ConsumerEventListener(container, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> onPlayerLogout(event), this));
	}
	
	public void shutdown()
	{
		Containers.Global().removeListenerIf(listener -> listener.getOwner() == this);
	}
	
	private void onPacketReceived(OnPacketReceived event)
	{
		handlePacket(event.getClient(), event.getData(), true);
	}
	
	private void onPacketSent(OnPacketSent event)
	{
		handlePacket(event.getClient(), event.getData(), false);
	}
	
	private void onPlayerLogout(OnPlayerLogout event)
	{
		final PlayerInstance player = event.getActiveChar();
		final L2GameClient client = player.getClient();
		if (client == null)
		{
			return;
		}
		
		final IPacketHandler handler = _logs.remove(client.getObjectId());
		if (handler != null)
		{
			handler.notifyTerminate();
			LOGGER.info("Ending log session for: {}", player);
		}
	}
	
	private final void handlePacket(L2GameClient client, byte[] data, boolean clientSide)
	{
		if (!_logs.containsKey(client.getObjectId()))
		{
			if (data.length > 0)
			{
				final int opCode = data[0] & 0xFF;
				if (opCode == IncomingPackets.PROTOCOL_VERSION.getPacketId()) // Create new session only when protocol version arrive!
				{
					_logs.put(client.getObjectId(), new YAL2Logger(client));
					LOGGER.info("Starting log session for: {}", client);
				}
			}
		}
		
		final IPacketHandler handler = _logs.get(client.getObjectId());
		if (handler != null)
		{
			handler.handlePacket(data, clientSide);
		}
	}
	
	public static final Yal2LoggerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Yal2LoggerManager INSTANCE = new Yal2LoggerManager();
	}
}