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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author -Wooden-
 * @author UnAfraid, mrTJO
 */
public class PackageSendableList extends AbstractItemPacket
{
	private final Collection<ItemInstance> _items;
	private final int _objectId;
	private final long _adena;
	
	public PackageSendableList(PlayerInstance player, int objectId)
	{
		_items = player.getInventory().getAvailableItems(true, true, true);
		_objectId = objectId;
		_adena = player.getAdena();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PACKAGE_SENDABLE_LIST.writeId(packet);
		
		packet.writeD(_objectId);
		packet.writeQ(_adena);
		packet.writeD(_items.size());
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
			packet.writeD(item.getObjectId());
		}
		return true;
	}
}
