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

import java.util.EnumMap;
import java.util.Map;

import org.l2junity.gameserver.model.skills.SkillCastingType;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ActionFailed implements IClientOutgoingPacket
{
	public static final ActionFailed STATIC_PACKET = new ActionFailed();
	private static final Map<SkillCastingType, ActionFailed> STATIC_PACKET_BY_CASTING_TYPE = new EnumMap<>(SkillCastingType.class);
	
	static
	{
		for (SkillCastingType castingType : SkillCastingType.values())
		{
			STATIC_PACKET_BY_CASTING_TYPE.put(castingType, new ActionFailed(castingType.getClientBarId()));
		}
	}
	
	private final int _castingType;
	
	private ActionFailed()
	{
		_castingType = 0;
	}
	
	private ActionFailed(int castingType)
	{
		_castingType = castingType;
	}
	
	public static ActionFailed get(SkillCastingType castingType)
	{
		return STATIC_PACKET_BY_CASTING_TYPE.getOrDefault(castingType, STATIC_PACKET);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ACTION_FAIL.writeId(packet);
		
		packet.writeD(_castingType); // MagicSkillUse castingType
		return true;
	}
}
