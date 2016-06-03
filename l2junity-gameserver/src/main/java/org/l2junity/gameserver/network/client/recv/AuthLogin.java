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
import org.l2junity.gameserver.LoginServerThread;
import org.l2junity.gameserver.LoginServerThread.SessionKey;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AuthLogin implements IClientIncomingPacket
{
	
	// loginName + keys must match what the loginserver used.
	private String _loginName;
	/*
	 * private final long _key1; private final long _key2; private final long _key3; private final long _key4;
	 */
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_loginName = packet.readS().toLowerCase();
		_playKey2 = packet.readD();
		_playKey1 = packet.readD();
		_loginKey1 = packet.readD();
		_loginKey2 = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if (_loginName.isEmpty() || !client.isProtocolOk())
		{
			client.close(null);
			return;
		}
		
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		if (Config.DEBUG)
		{
			_log.info("user:" + _loginName);
			_log.info("key:" + key);
		}
		
		// avoid potential exploits
		if (client.getAccountName() == null)
		{
			// Preventing duplicate login in case client login server socket was disconnected or this packet was not sent yet
			if (LoginServerThread.getInstance().addGameServerLogin(_loginName, client))
			{
				client.setAccountName(_loginName);
				LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
			}
			else
			{
				client.close(null);
			}
		}
	}
}
