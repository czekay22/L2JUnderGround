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

import org.l2junity.gameserver.instancemanager.MatchingRoomManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.matching.MatchingRoom;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * @author Gnacik
 */
public final class RequestPartyMatchDetail implements IClientIncomingPacket
{
	private int _roomId;
	private int _location;
	private int _level;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_roomId = packet.readD();
		_location = packet.readD();
		_level = packet.readD();
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
		
		if (activeChar.isInMatchingRoom())
		{
			return;
		}
		
		final MatchingRoom room = _roomId > 0 ? MatchingRoomManager.getInstance().getPartyMathchingRoom(_roomId) : MatchingRoomManager.getInstance().getPartyMathchingRoom(_location, _level);
		
		if (room != null)
		{
			room.addMember(activeChar);
		}
	}
	
}
