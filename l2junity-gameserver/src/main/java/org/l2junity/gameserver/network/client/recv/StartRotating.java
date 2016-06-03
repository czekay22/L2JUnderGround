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
import org.l2junity.gameserver.network.client.send.StartRotation;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class StartRotating implements IClientIncomingPacket
{
	private int _degree;
	private int _side;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_degree = packet.readD();
		_side = packet.readD();
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
		
		if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
		{
			activeChar.getAirShip().broadcastPacket(new StartRotation(activeChar.getAirShip().getObjectId(), _degree, _side, 0));
		}
		else
		{
			activeChar.broadcastPacket(new StartRotation(activeChar.getObjectId(), _degree, _side, 0));
		}
	}
}
