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
package org.l2junity.gameserver.network.client.recv.shuttle;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.shuttle.ExStopMoveInShuttle;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class CannotMoveAnymoreInShuttle implements IClientIncomingPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_boatId = packet.readD();
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
		_heading = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isInShuttle())
		{
			if (activeChar.getShuttle().getObjectId() == _boatId)
			{
				activeChar.setInVehiclePosition(new Location(_x, _y, _z));
				activeChar.setHeading(_heading);
				activeChar.broadcastPacket(new ExStopMoveInShuttle(activeChar, _boatId));
			}
		}
	}
}
