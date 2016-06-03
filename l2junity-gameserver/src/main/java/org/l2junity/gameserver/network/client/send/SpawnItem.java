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

import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class SpawnItem implements IClientOutgoingPacket
{
	private final int _objectId;
	private int _itemId;
	private final int _x, _y, _z;
	private int _stackable;
	private long _count;
	
	public SpawnItem(WorldObject obj)
	{
		_objectId = obj.getObjectId();
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		
		if (obj.isItem())
		{
			ItemInstance item = (ItemInstance) obj;
			_itemId = item.getDisplayId();
			_stackable = item.isStackable() ? 0x01 : 0x00;
			_count = item.getCount();
		}
		else
		{
			_itemId = obj.getPoly().getPolyId();
			_stackable = 0;
			_count = 1;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SPAWN_ITEM.writeId(packet);
		
		packet.writeD(_objectId);
		packet.writeD(_itemId);
		
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		// only show item count if it is a stackable item
		packet.writeD(_stackable);
		packet.writeQ(_count);
		packet.writeD(0x00); // c2
		return true;
	}
}
