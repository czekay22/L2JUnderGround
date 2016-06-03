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
public class LoginOk implements IOutgoingPacket
{
	private final long _loginSessionId;
	
	public LoginOk(long loginSessionId)
	{
		_loginSessionId = loginSessionId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0x03);
		packet.writeQ(_loginSessionId);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		
		// forbidden server makes name red
		// its a mask
		// 0x01 is server id 1
		// 0x02 is server id 2
		// 0x04 is server id 3
		// etc
		packet.writeD(1);
		packet.writeD(0xFFFFFFFF);
		packet.writeD(0xFFFFFFFF);
		packet.writeD(0xFFFFFFFF);
		return true;
	}
}
