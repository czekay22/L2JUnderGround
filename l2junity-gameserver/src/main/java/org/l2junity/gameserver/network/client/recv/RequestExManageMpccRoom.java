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

import org.l2junity.gameserver.enums.MatchingRoomType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.matching.CommandChannelMatchingRoom;
import org.l2junity.gameserver.model.matching.MatchingRoom;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExMPCCRoomInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author Sdw
 */
public class RequestExManageMpccRoom implements IClientIncomingPacket
{
	private int _roomId;
	private int _maxMembers;
	private int _minLevel;
	private int _maxLevel;
	private String _title;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_roomId = packet.readD();
		_maxMembers = packet.readD();
		_minLevel = packet.readD();
		_maxLevel = packet.readD();
		packet.readD(); // Party Distrubtion Type
		_title = packet.readS();
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
		
		final MatchingRoom room = activeChar.getMatchingRoom();
		if ((room == null) || (room.getId() != _roomId) || (room.getRoomType() != MatchingRoomType.COMMAND_CHANNEL) || (room.getLeader() != activeChar))
		{
			return;
		}
		
		room.setTitle(_title);
		room.setMaxMembers(_maxMembers);
		room.setMinLvl(_minLevel);
		room.setMaxLvl(_maxLevel);
		
		room.getMembers().forEach(p -> p.sendPacket(new ExMPCCRoomInfo((CommandChannelMatchingRoom) room)));
		
		activeChar.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_MATCHING_ROOM_INFORMATION_WAS_EDITED);
	}
}
