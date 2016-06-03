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

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle implements IClientOutgoingPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Location _destination;
	private final Location _origin;
	
	/**
	 * @param player
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(PlayerInstance player, Location destination, Location origin)
	{
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MOVE_TO_LOCATION_IN_VEHICLE.writeId(packet);
		
		packet.writeD(_charObjId);
		packet.writeD(_boatId);
		packet.writeD(_destination.getX());
		packet.writeD(_destination.getY());
		packet.writeD(_destination.getZ());
		packet.writeD(_origin.getX());
		packet.writeD(_origin.getY());
		packet.writeD(_origin.getZ());
		return true;
	}
}
