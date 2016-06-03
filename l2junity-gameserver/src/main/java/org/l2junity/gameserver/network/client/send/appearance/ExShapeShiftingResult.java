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
package org.l2junity.gameserver.network.client.send.appearance;

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public class ExShapeShiftingResult implements IClientOutgoingPacket
{
	public static int RESULT_FAILED = 0x00;
	public static int RESULT_SUCCESS = 0x01;
	
	public static ExShapeShiftingResult FAILED = new ExShapeShiftingResult(RESULT_FAILED, 0, 0);
	
	private final int _result;
	private final int _targetItemId;
	private final int _extractItemId;
	
	public ExShapeShiftingResult(int result, int targetItemId, int extractItemId)
	{
		_result = result;
		_targetItemId = targetItemId;
		_extractItemId = extractItemId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHAPE_SHIFTING_RESULT.writeId(packet);
		
		packet.writeD(_result);
		packet.writeD(_targetItemId);
		packet.writeD(_extractItemId);
		return true;
	}
}
