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

import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author zabbix
 */
public class PartyMemberPosition implements IClientOutgoingPacket
{
	private final Map<Integer, Location> locations = new HashMap<>();
	
	public PartyMemberPosition(Party party)
	{
		reuse(party);
	}
	
	public void reuse(Party party)
	{
		locations.clear();
		for (PlayerInstance member : party.getMembers())
		{
			if (member == null)
			{
				continue;
			}
			locations.put(member.getObjectId(), member.getLocation());
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_MEMBER_POSITION.writeId(packet);
		
		packet.writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			packet.writeD(entry.getKey());
			packet.writeD(loc.getX());
			packet.writeD(loc.getY());
			packet.writeD(loc.getZ());
		}
		return true;
	}
}
