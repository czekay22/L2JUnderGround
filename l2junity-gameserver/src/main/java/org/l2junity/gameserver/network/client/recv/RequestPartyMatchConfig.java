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

import org.l2junity.gameserver.instancemanager.MatchingRoomManager;
import org.l2junity.gameserver.model.CommandChannel;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.matching.CommandChannelMatchingRoom;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ListPartyWaiting;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

public final class RequestPartyMatchConfig implements IClientIncomingPacket
{
	private int _page, _location, _level;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_page = packet.readD();
		_location = packet.readD();
		_level = packet.readD();
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
		
		final Party party = activeChar.getParty();
		final CommandChannel cc = party == null ? null : party.getCommandChannel();
		
		if ((party != null) && (cc != null) && (cc.getLeader() == activeChar))
		{
			if (activeChar.getMatchingRoom() == null)
			{
				activeChar.setMatchingRoom(new CommandChannelMatchingRoom(activeChar.getName(), party.getDistributionType().ordinal(), 1, activeChar.getLevel(), 50, activeChar));
			}
		}
		else if ((cc != null) && (cc.getLeader() != activeChar))
		{
			activeChar.sendPacket(SystemMessageId.THE_COMMAND_CHANNEL_AFFILIATED_PARTY_S_PARTY_MEMBER_CANNOT_USE_THE_MATCHING_SCREEN);
		}
		else if ((party != null) && (party.getLeader() != activeChar))
		{
			activeChar.sendPacket(SystemMessageId.THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_IS_NOT_PART_OF_A_PARTY);
		}
		else
		{
			MatchingRoomManager.getInstance().addToWaitingList(activeChar);
			activeChar.sendPacket(new ListPartyWaiting(_level, _location, _page));
		}
	}
	
}
