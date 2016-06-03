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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.l2junity.gameserver.enums.MatchingMemberType;
import org.l2junity.gameserver.instancemanager.InstanceManager;
import org.l2junity.gameserver.instancemanager.MapRegionManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.matching.PartyMatchingRoom;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Gnacik
 */
public class ExPartyRoomMember implements IClientOutgoingPacket
{
	private final PartyMatchingRoom _room;
	private final MatchingMemberType _type;
	
	public ExPartyRoomMember(PlayerInstance player, PartyMatchingRoom room)
	{
		_room = room;
		_type = room.getMemberType(player);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PARTY_ROOM_MEMBER.writeId(packet);
		
		packet.writeD(_type.ordinal());
		packet.writeD(_room.getMembersCount());
		for (PlayerInstance member : _room.getMembers())
		{
			packet.writeD(member.getObjectId());
			packet.writeS(member.getName());
			packet.writeD(member.getActiveClass());
			packet.writeD(member.getLevel());
			packet.writeD(MapRegionManager.getInstance().getBBs(member.getLocation()));
			packet.writeD(_room.getMemberType(member).ordinal());
			final Map<Integer, Long> _instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(member);
			packet.writeD(_instanceTimes.size());
			for (Entry<Integer, Long> entry : _instanceTimes.entrySet())
			{
				final long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
				packet.writeD(entry.getKey());
				packet.writeD((int) instanceTime);
			}
		}
		return true;
	}
}