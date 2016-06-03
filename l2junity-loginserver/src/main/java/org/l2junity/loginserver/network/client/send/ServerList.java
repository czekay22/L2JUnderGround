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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.l2junity.loginserver.model.GameServer;
import org.l2junity.loginserver.model.enums.AgeLimit;
import org.l2junity.loginserver.model.enums.ServerType;
import org.l2junity.loginserver.network.client.ClientHandler;
import org.l2junity.network.IOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author NosBit
 */
public class ServerList implements IOutgoingPacket
{
	private final ClientHandler _client;
	
	public ServerList(ClientHandler client)
	{
		_client = client;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		// TODO: Implement me
		packet.writeC(0x04);
		
		Set<GameServer> gameservers = new HashSet<>();
		gameservers.add(new GameServer((short) 1, "1", true, AgeLimit.EIGHTEEN, new HashSet<>(Arrays.asList(ServerType.FREE))));
		gameservers.add(new GameServer((short) 2, "2", true, AgeLimit.EIGHTEEN, new HashSet<>(Arrays.asList(ServerType.FREE))));
		packet.writeC(gameservers.size());
		packet.writeC(_client.getAccount().getLastServerId());
		
		for (GameServer gameServer : gameservers)
		{
			packet.writeC(gameServer.getId());
			
			packet.writeC(127); // IP
			packet.writeC(0); // IP
			packet.writeC(0); // IP
			packet.writeC(1); // IP
			packet.writeD(7777); // Port
			
			packet.writeC(gameServer.getAgeLimit().getAge());
			packet.writeC(0); // PK Enabled - Unused by client
			packet.writeH(1); // Player Count
			packet.writeH(1); // Player Limit
			packet.writeC(0); // ServerState.OFFLINE(0), ONLINE(1)
			packet.writeD(gameServer.getServerTypesMask());
			packet.writeC(0); // Puts [NULL] in front of name due to missing file in NA client
		}
		
		packet.writeH(0); // Unused by client
		
		packet.writeC(2);
		for (int i = 1; i <= 2; i++)
		{
			packet.writeC(i); // Server ID
			packet.writeC(127); // Character Count
			packet.writeC(2); // Deleted Character Count
			for (int j = 1; j <= 2; j++)
			{
				packet.writeD(j);
			}
		}
		
		return true;
	}
}
