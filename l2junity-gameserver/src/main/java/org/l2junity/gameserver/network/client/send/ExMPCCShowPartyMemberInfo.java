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
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author chris_00
 */
public class ExMPCCShowPartyMemberInfo implements IClientOutgoingPacket
{
	private final Party _party;
	
	public ExMPCCShowPartyMemberInfo(Party party)
	{
		_party = party;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MPCCSHOW_PARTY_MEMBER_INFO.writeId(packet);
		
		packet.writeD(_party.getMemberCount());
		for (PlayerInstance pc : _party.getMembers())
		{
			packet.writeS(pc.getName());
			packet.writeD(pc.getObjectId());
			packet.writeD(pc.getClassId().getId());
		}
		return true;
	}
}
