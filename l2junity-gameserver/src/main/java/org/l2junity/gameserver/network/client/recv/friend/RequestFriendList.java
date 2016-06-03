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
package org.l2junity.gameserver.network.client.recv.friend;

import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendList implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
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
		
		SystemMessage sm;
		
		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIENDS_LIST);
		
		PlayerInstance friend = null;
		for (int id : activeChar.getFriendList())
		{
			// int friendId = rset.getInt("friendId");
			String friendName = CharNameTable.getInstance().getNameById(id);
			
			if (friendName == null)
			{
				continue;
			}
			
			friend = World.getInstance().getPlayer(friendName);
			
			if ((friend == null) || !friend.isOnline())
			{
				// (Currently: Offline)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				// (Currently: Online)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_ONLINE);
				sm.addString(friendName);
			}
			
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(SystemMessageId.EMPTY3);
	}
}
