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
package org.l2junity.gameserver.network.client.recv;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.enums.ItemLocation;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * Format:(ch) d[dd]
 * @author -Wooden-
 */
public final class RequestSaveInventoryOrder implements IClientIncomingPacket
{
	private List<InventoryOrder> _order;
	
	/** client limit */
	private static final int LIMIT = 125;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		int sz = packet.readD();
		sz = Math.min(sz, LIMIT);
		_order = new ArrayList<>(sz);
		for (int i = 0; i < sz; i++)
		{
			int objectId = packet.readD();
			int order = packet.readD();
			_order.add(new InventoryOrder(objectId, order));
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		if (player != null)
		{
			Inventory inventory = player.getInventory();
			for (InventoryOrder order : _order)
			{
				ItemInstance item = inventory.getItemByObjectId(order.objectID);
				if ((item != null) && (item.getItemLocation() == ItemLocation.INVENTORY))
				{
					item.setItemLocation(ItemLocation.INVENTORY, order.order);
				}
			}
		}
	}
	
	private static class InventoryOrder
	{
		int order;
		
		int objectID;
		
		public InventoryOrder(int id, int ord)
		{
			objectID = id;
			order = ord;
		}
	}
}
