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

import javax.crypto.SecretKey;

import org.l2junity.loginserver.network.client.crypt.ScrambledRSAKeyPair;
import org.l2junity.network.IOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author NosBit
 */
public class Init implements IOutgoingPacket
{
	private final int _connectionId;
	private final byte[] _publicKey;
	private final byte[] _blowfishKey;
	
	public Init(int connectionId, ScrambledRSAKeyPair scrambledRSAKeyPair, SecretKey blowfishKey)
	{
		_connectionId = connectionId;
		_publicKey = scrambledRSAKeyPair.getScrambledModulus();
		_blowfishKey = blowfishKey.getEncoded();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0x00);
		packet.writeD(_connectionId); // connection id
		packet.writeD(0x0000C621); // protocol revision
		packet.writeB(_publicKey); // RSA Public Key
		packet.writeD(0x29DD954E); // UNKNOWN GAMEGUARD
		packet.writeD(0x77C39CFC); // UNKNOWN GAMEGUARD
		packet.writeD(0x97ADB620); // UNKNOWN GAMEGUARD
		packet.writeD(0x07BDE0F7); // UNKNOWN GAMEGUARD
		packet.writeB(_blowfishKey); // String (BlowFishkey)
		packet.writeC(0x00); // String (NULL termination)
		return true;
	}
}
