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

import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * Format: (c) dddd d: dx d: dy d: dz d: AirShip id ??
 * @author -Wooden-
 */
public class ExGetOnAirShip implements IClientIncomingPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _shipId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
		_shipId = packet.readD();
		return false;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		_log.info("[T1:ExGetOnAirShip] x: " + _x);
		_log.info("[T1:ExGetOnAirShip] y: " + _y);
		_log.info("[T1:ExGetOnAirShip] z: " + _z);
		_log.info("[T1:ExGetOnAirShip] ship ID: " + _shipId);
	}
}
