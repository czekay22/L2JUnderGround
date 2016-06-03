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

import java.util.List;
import java.util.stream.Collectors;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ItemList extends AbstractItemPacket
{
	private final PlayerInstance _activeChar;
	private final List<ItemInstance> _items;
	private final boolean _showWindow;
	
	public ItemList(PlayerInstance activeChar, boolean showWindow)
	{
		_activeChar = activeChar;
		_showWindow = showWindow;
		_items = activeChar.getInventory().getItems(item -> !item.isQuestItem()).stream().collect(Collectors.toList());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ITEM_LIST.writeId(packet);
		
		packet.writeH(_showWindow ? 0x01 : 0x00);
		packet.writeH(_items.size());
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
		}
		writeInventoryBlock(packet, _activeChar.getInventory());
		return true;
	}
}
