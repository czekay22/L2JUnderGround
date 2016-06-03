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
package org.l2junity.loginserver.network.loginserverpackets;

import org.l2junity.loginserver.L2LoginServer;
import org.l2junity.util.network.BaseSendablePacket;

/**
 * @author -Wooden-
 */
public class InitLS extends BaseSendablePacket
{
	// ID 0x00
	// format
	// d proto rev
	// d key size
	// b key
	
	public InitLS(byte[] publickey)
	{
		writeC(0x00);
		writeD(L2LoginServer.PROTOCOL_REV);
		writeD(publickey.length);
		writeB(publickey);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
