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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.l2junity.gameserver.model.ItemInfo;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public abstract class AbstractInventoryUpdate extends AbstractItemPacket
{
	private final Map<Integer, ItemInfo> _items = new ConcurrentSkipListMap<>();
	
	public AbstractInventoryUpdate()
	{
	}
	
	public AbstractInventoryUpdate(ItemInstance item)
	{
		addItem(item);
	}
	
	public AbstractInventoryUpdate(List<ItemInfo> items)
	{
		for (ItemInfo item : items)
		{
			_items.put(item.getObjectId(), item);
		}
	}
	
	public final void addItem(ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item));
	}
	
	public final void addNewItem(ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 1));
	}
	
	public final void addModifiedItem(ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 2));
	}
	
	public final void addRemovedItem(ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 3));
	}
	
	public final void addItems(List<ItemInstance> items)
	{
		for (ItemInstance item : items)
		{
			_items.put(item.getObjectId(), new ItemInfo(item));
		}
	}
	
	public final Collection<ItemInfo> getItems()
	{
		return _items.values();
	}
	
	protected final void writeItems(PacketWriter packet)
	{
		packet.writeH(_items.size());
		for (ItemInfo item : _items.values())
		{
			packet.writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeItem(packet, item);
		}
	}
}
