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
package org.l2junity.gameserver.network.client.send;

import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class TeleportToLocation implements IClientOutgoingPacket
{
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	
	public TeleportToLocation(WorldObject obj, int x, int y, int z, int heading)
	{
		_targetObjId = obj.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.TELEPORT_TO_LOCATION.writeId(packet);
		
		packet.writeD(_targetObjId);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(0x00); // isValidation ??
		packet.writeD(_heading);
		packet.writeD(0x00); // Unknown
		return true;
	}
}
