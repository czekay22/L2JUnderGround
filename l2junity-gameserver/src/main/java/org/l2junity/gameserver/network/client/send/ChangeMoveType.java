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

public class ChangeMoveType implements IClientOutgoingPacket
{
	public static final int WALK = 0;
	public static final int RUN = 1;
	
	private final int _charObjId;
	private final boolean _running;
	
	public ChangeMoveType(Creature character)
	{
		_charObjId = character.getObjectId();
		_running = character.isRunning();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHANGE_MOVE_TYPE.writeId(packet);
		
		packet.writeD(_charObjId);
		packet.writeD(_running ? RUN : WALK);
		packet.writeD(0); // c2
		return true;
	}
}
