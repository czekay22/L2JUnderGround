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

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class GMViewItemList extends AbstractItemPacket
{
	private final List<ItemInstance> _items = new ArrayList<>();
	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(PlayerInstance cha)
	{
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
		for (ItemInstance item : cha.getInventory().getItems())
		{
			_items.add(item);
		}
	}
	
	public GMViewItemList(L2PetInstance cha)
	{
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
		for (ItemInstance item : cha.getInventory().getItems())
		{
			_items.add(item);
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GM_VIEW_ITEM_LIST.writeId(packet);
		
		packet.writeS(_playerName);
		packet.writeD(_limit); // inventory limit
		packet.writeH(0x01); // show window ??
		packet.writeH(_items.size());
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
		}
		return true;
	}
}
