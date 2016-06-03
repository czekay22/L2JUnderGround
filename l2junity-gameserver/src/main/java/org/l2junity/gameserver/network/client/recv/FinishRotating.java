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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.StopRotation;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class FinishRotating implements IClientIncomingPacket
{
	private int _degree;
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_degree = packet.readD();
		_unknown = packet.readD();
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
		
		StopRotation sr;
		if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
		{
			activeChar.getAirShip().setHeading(_degree);
			sr = new StopRotation(activeChar.getAirShip().getObjectId(), _degree, 0);
			activeChar.getAirShip().broadcastPacket(sr);
		}
		else
		{
			sr = new StopRotation(activeChar.getObjectId(), _degree, 0);
			activeChar.broadcastPacket(sr);
		}
	}
}
