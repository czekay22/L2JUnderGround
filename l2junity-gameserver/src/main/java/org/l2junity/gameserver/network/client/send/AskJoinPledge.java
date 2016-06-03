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

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class AskJoinPledge implements IClientOutgoingPacket
{
	private final int _requestorObjId;
	private final String _subPledgeName;
	private final int _pledgeType;
	private final String _pledgeName;
	
	public AskJoinPledge(int requestorObjId, String subPledgeName, int pledgeType, String pledgeName)
	{
		_requestorObjId = requestorObjId;
		_subPledgeName = subPledgeName;
		_pledgeType = pledgeType;
		_pledgeName = pledgeName;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ASK_JOIN_PLEDGE.writeId(packet);
		
		packet.writeD(_requestorObjId);
		if (_subPledgeName != null)
		{
			packet.writeS(_pledgeType > 0 ? _subPledgeName : _pledgeName);
		}
		packet.writeS(_pledgeName);
		if (_pledgeType != 0)
		{
			packet.writeD(_pledgeType);
		}
		return true;
	}
}
