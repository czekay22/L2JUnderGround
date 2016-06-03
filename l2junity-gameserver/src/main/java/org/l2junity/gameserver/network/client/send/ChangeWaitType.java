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

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class ChangeWaitType implements IClientOutgoingPacket
{
	private final int _charObjId;
	private final int _moveType;
	private final int _x, _y, _z;
	
	public static final int WT_SITTING = 0;
	public static final int WT_STANDING = 1;
	public static final int WT_START_FAKEDEATH = 2;
	public static final int WT_STOP_FAKEDEATH = 3;
	
	public ChangeWaitType(Creature character, int newMoveType)
	{
		_charObjId = character.getObjectId();
		_moveType = newMoveType;
		
		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHANGE_WAIT_TYPE.writeId(packet);
		
		packet.writeD(_charObjId);
		packet.writeD(_moveType);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		return true;
	}
}
