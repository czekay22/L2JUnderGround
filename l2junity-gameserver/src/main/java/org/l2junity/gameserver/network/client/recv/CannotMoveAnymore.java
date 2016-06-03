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

import org.l2junity.Config;
import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class CannotMoveAnymore implements IClientIncomingPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
		_heading = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.debug("client: x:" + _x + " y:" + _y + " z:" + _z + " server x:" + player.getX() + " y:" + player.getY() + " z:" + player.getZ());
		}
		if (player.getAI() != null)
		{
			player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new Location(_x, _y, _z, _heading));
		}
		/*
		 * if (player.getParty() != null) { player.getParty().broadcastToPartyMembers(player, new PartyMemberPosition(player)); }
		 */
		
		// player.stopMove();
		//
		// if (Config.DEBUG)
		// LOGGER.debug("client: x:"+_x+" y:"+_y+" z:"+_z+
		// " server x:"+player.getX()+" y:"+player.getZ()+" z:"+player.getZ());
		// StopMove smwl = new StopMove(player);
		// client.getActiveChar().sendPacket(smwl);
		// client.getActiveChar().broadcastPacket(smwl);
		//
		// StopRotation sr = new StopRotation(client.getActiveChar(),
		// _heading);
		// client.getActiveChar().sendPacket(sr);
		// client.getActiveChar().broadcastPacket(sr);
	}
}
