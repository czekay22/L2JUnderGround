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

import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class DropItem implements IClientOutgoingPacket
{
	private final ItemInstance _item;
	private final int _charObjId;
	
	/**
	 * Constructor of the DropItem server packet
	 * @param item : L2ItemInstance designating the item
	 * @param playerObjId : int designating the player ID who dropped the item
	 */
	public DropItem(ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.DROP_ITEM.writeId(packet);
		
		packet.writeD(_charObjId);
		packet.writeD(_item.getObjectId());
		packet.writeD(_item.getDisplayId());
		
		packet.writeD(_item.getX());
		packet.writeD(_item.getY());
		packet.writeD(_item.getZ());
		// only show item count if it is a stackable item
		packet.writeC(_item.isStackable() ? 0x01 : 0x00);
		packet.writeQ(_item.getCount());
		
		packet.writeC(0x00);
		// packet.writeD(0x01); if above C == true (1) then packet.readD()
		return true;
	}
}
