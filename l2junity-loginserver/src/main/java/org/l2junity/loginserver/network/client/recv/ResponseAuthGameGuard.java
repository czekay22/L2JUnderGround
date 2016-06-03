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
package org.l2junity.loginserver.network.client.recv;

import org.l2junity.loginserver.network.client.ClientHandler;
import org.l2junity.loginserver.network.client.ConnectionState;
import org.l2junity.loginserver.network.client.send.AuthGameGuard;
import org.l2junity.loginserver.network.client.send.LoginFail2;
import org.l2junity.network.IIncomingPacket;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class ResponseAuthGameGuard implements IIncomingPacket<ClientHandler>
{
	private int _connectionId;
	private byte[] _gameGuard;
	
	@Override
	public boolean read(ClientHandler client, PacketReader packet)
	{
		_connectionId = packet.readD();
		_gameGuard = packet.readB(16);
		return true;
	}
	
	@Override
	public void run(ClientHandler client)
	{
		if (_connectionId == client.getConnectionId())
		{
			client.setGameGuard(_gameGuard);
			client.setConnectionState(ConnectionState.AUTHED_GG);
			client.sendPacket(new AuthGameGuard(_connectionId));
		}
		else
		{
			client.close(LoginFail2.ACCESS_FAILED);
		}
	}
}
