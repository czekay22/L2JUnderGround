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
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author KenM
 */
public final class FlyToLocation implements IClientOutgoingPacket
{
	private final int _destX, _destY, _destZ;
	private final int _chaObjId, _chaX, _chaY, _chaZ;
	private final FlyType _type;
	private int _flySpeed;
	private int _flyDelay;
	private int _animationSpeed;
	
	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY,
		CHARGE,
		PUSH_HORIZONTAL,
		JUMP_EFFECTED,
		NOT_USED,
		PUSH_DOWN_HORIZONTAL,
		WARP_BACK,
		WARP_FORWARD
	}
	
	public FlyToLocation(Creature cha, int destX, int destY, int destZ, FlyType type)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
	}
	
	public FlyToLocation(Creature cha, int destX, int destY, int destZ, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
		_flySpeed = flySpeed;
		_flyDelay = flyDelay;
		_animationSpeed = animationSpeed;
	}
	
	public FlyToLocation(Creature cha, ILocational dest, FlyType type)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
	}
	
	public FlyToLocation(Creature cha, ILocational dest, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type, flySpeed, flyDelay, animationSpeed);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.FLY_TO_LOCATION.writeId(packet);
		
		packet.writeD(_chaObjId);
		packet.writeD(_destX);
		packet.writeD(_destY);
		packet.writeD(_destZ);
		packet.writeD(_chaX);
		packet.writeD(_chaY);
		packet.writeD(_chaZ);
		packet.writeD(_type.ordinal());
		packet.writeD(_flySpeed);
		packet.writeD(_flyDelay);
		packet.writeD(_animationSpeed);
		return true;
	}
}
