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
package org.l2junity.gameserver.network.client.send.commission;

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author NosBit
 */
public class ExResponseCommissionInfo implements IClientOutgoingPacket
{
	public static final ExResponseCommissionInfo EMPTY = new ExResponseCommissionInfo();
	
	private final int _result;
	private final int _itemId;
	private final long _presetPricePerUnit;
	private final long _presetAmount;
	private final int _presetDurationType;
	
	private ExResponseCommissionInfo()
	{
		_result = 0;
		_itemId = 0;
		_presetPricePerUnit = 0;
		_presetAmount = 0;
		_presetDurationType = -1;
	}
	
	public ExResponseCommissionInfo(int itemId, long presetPricePerUnit, long presetAmount, int presetDurationType)
	{
		_result = 1;
		_itemId = itemId;
		_presetPricePerUnit = presetPricePerUnit;
		_presetAmount = presetAmount;
		_presetDurationType = presetDurationType;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RESPONSE_COMMISSION_INFO.writeId(packet);
		
		packet.writeD(_result);
		packet.writeD(_itemId);
		packet.writeQ(_presetPricePerUnit);
		packet.writeQ(_presetAmount);
		packet.writeD(_presetDurationType);
		return true;
	}
}
