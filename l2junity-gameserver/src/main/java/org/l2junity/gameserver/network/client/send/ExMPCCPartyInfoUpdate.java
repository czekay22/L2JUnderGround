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
package org.l2junity.gameserver.network.client.send;

import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author chris_00
 */
public class ExMPCCPartyInfoUpdate implements IClientOutgoingPacket
{
	private final int _mode, _LeaderOID, _memberCount;
	private final String _name;
	
	/**
	 * @param party
	 * @param mode 0 = Remove, 1 = Add
	 */
	public ExMPCCPartyInfoUpdate(Party party, int mode)
	{
		_name = party.getLeader().getName();
		_LeaderOID = party.getLeaderObjectId();
		_memberCount = party.getMemberCount();
		_mode = mode;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MPCCPARTY_INFO_UPDATE.writeId(packet);
		
		packet.writeS(_name);
		packet.writeD(_LeaderOID);
		packet.writeD(_memberCount);
		packet.writeD(_mode); // mode 0 = Remove Party, 1 = AddParty, maybe more...
		return true;
	}
}
