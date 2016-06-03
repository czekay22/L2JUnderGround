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

import org.l2junity.Config;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.L2FriendSay;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author Tempy
 */
public final class RequestSendFriendMsg implements IClientIncomingPacket
{
	private static Logger _logChat = LoggerFactory.getLogger("chat");
	
	private String _message;
	private String _reciever;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_message = packet.readS();
		_reciever = packet.readS();
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
		
		if ((_message == null) || _message.isEmpty() || (_message.length() > 300))
		{
			return;
		}
		
		final PlayerInstance targetPlayer = World.getInstance().getPlayer(_reciever);
		if ((targetPlayer == null) || !targetPlayer.getFriendList().contains(activeChar.getObjectId()))
		{
			activeChar.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			_logChat.info("PRIV_MSG [{} to {}] {}", activeChar, targetPlayer, _message);
		}
		
		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
	}
}
