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
package org.l2junity.gameserver.network.client.send.fishing;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author -Wooden-
 */
public class ExFishingStart implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	private final int _fishType;
	private final int _baitType;
	private final ILocational _baitLocation;
	
	/**
	 * @param player
	 * @param fishType
	 * @param baitType - 0 = newbie, 1 = normal, 2 = night
	 * @param baitLocation
	 */
	public ExFishingStart(PlayerInstance player, int fishType, int baitType, ILocational baitLocation)
	{
		_player = player;
		_fishType = fishType;
		_baitType = baitType;
		_baitLocation = baitLocation;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_FISHING_START.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeC(_fishType);
		packet.writeD(_baitLocation.getX());
		packet.writeD(_baitLocation.getY());
		packet.writeD(_baitLocation.getZ());
		packet.writeC(_baitType);
		return true;
	}
}
