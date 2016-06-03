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

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Maktakien
 */
public class GetOffVehicle implements IClientOutgoingPacket
{
	private final int _charObjId, _boatObjId, _x, _y, _z;
	
	/**
	 * @param charObjId
	 * @param boatObjId
	 * @param x
	 * @param y
	 * @param z
	 */
	public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z)
	{
		_charObjId = charObjId;
		_boatObjId = boatObjId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GET_OFF_VEHICLE.writeId(packet);
		
		packet.writeD(_charObjId);
		packet.writeD(_boatObjId);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		return true;
	}
}
