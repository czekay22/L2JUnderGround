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

import org.l2junity.gameserver.enums.AttributeType;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class ExAttributeEnchantResult implements IClientOutgoingPacket
{
	private final int _result;
	private final int _isWeapon;
	private final int _type;
	private final int _before;
	private final int _after;
	private final int _successCount;
	private final int _failedCount;
	
	public ExAttributeEnchantResult(int result, boolean isWeapon, AttributeType type, int before, int after, int successCount, int failedCount)
	{
		_result = result;
		_isWeapon = isWeapon ? 1 : 0;
		_type = type.getClientId();
		_before = before;
		_after = after;
		_successCount = successCount;
		_failedCount = failedCount;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ATTRIBUTE_ENCHANT_RESULT.writeId(packet);
		
		packet.writeD(_result);
		packet.writeC(_isWeapon);
		packet.writeH(_type);
		packet.writeH(_before);
		packet.writeH(_after);
		packet.writeH(_successCount);
		packet.writeH(_failedCount);
		return true;
	}
}
