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
package org.l2junity.gameserver.network.client.recv;

import org.l2junity.gameserver.network.client.ExIncomingPackets;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.IIncomingPacket;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nos
 */
public class ExPacket implements IClientIncomingPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ExPacket.class);
	
	private ExIncomingPackets _exIncomingPacket;
	private IIncomingPacket<L2GameClient> _exPacket;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		int exPacketId = packet.readH() & 0xFFFF;
		if ((exPacketId < 0) || (exPacketId >= ExIncomingPackets.PACKET_ARRAY.length))
		{
			return false;
		}
		
		_exIncomingPacket = ExIncomingPackets.PACKET_ARRAY[exPacketId];
		if (_exIncomingPacket == null)
		{
			LOGGER.debug("{}: Unknown packet: {}", getClass().getSimpleName(), Integer.toHexString(exPacketId));
			return false;
		}
		
		_exPacket = _exIncomingPacket.newIncomingPacket();
		return (_exPacket != null) && _exPacket.read(client, packet);
	}
	
	@Override
	public void run(L2GameClient client) throws Exception
	{
		if (!_exIncomingPacket.getConnectionStates().contains(client.getConnectionState()))
		{
			LOGGER.debug("{}: Connection at invalid state: {} Required State: {}", _exIncomingPacket, client.getConnectionState(), _exIncomingPacket.getConnectionStates());
			return;
		}
		_exPacket.run(client);
	}
}
