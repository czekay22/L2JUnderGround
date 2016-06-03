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

/**
 * @author Migi
 */
public class ExChangePostState implements IClientOutgoingPacket
{
	private final boolean _receivedBoard;
	private final int[] _changedMsgIds;
	private final int _changeId;
	
	public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, int changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = changedMsgIds;
		_changeId = changeId;
	}
	
	public ExChangePostState(boolean receivedBoard, int changedMsgId, int changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = new int[]
		{
			changedMsgId
		};
		_changeId = changeId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CHANGE_POST_STATE.writeId(packet);
		
		packet.writeD(_receivedBoard ? 1 : 0);
		packet.writeD(_changedMsgIds.length);
		for (int postId : _changedMsgIds)
		{
			packet.writeD(postId); // postId
			packet.writeD(_changeId); // state
		}
		return true;
	}
}
