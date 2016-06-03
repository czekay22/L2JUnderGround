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
package org.l2junity.loginserver.network.client.send;

import org.l2junity.network.IOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author NosBit
 */
public class BlockedAccountWithMsg implements IOutgoingPacket
{
	private final String _message;
	
	public BlockedAccountWithMsg(String message)
	{
		_message = message;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2junity.network.IOutgoingPacket#write(org.l2junity.network.PacketWriter)
	 */
	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0x09);
		packet.writeC(1); // The following [hS] is read in a loop but only last one is displayed so i sent only 1.
		packet.writeH(1); // Unused by client
		packet.writeS(_message);
		return true;
	}
	
}
