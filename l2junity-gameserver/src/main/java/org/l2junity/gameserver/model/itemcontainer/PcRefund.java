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
package org.l2junity.gameserver.model.itemcontainer;

import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.enums.ItemLocation;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;

/**
 * @author DS
 */
public class PcRefund extends ItemContainer
{
	private final PlayerInstance _owner;
	
	public PcRefund(PlayerInstance owner)
	{
		_owner = owner;
	}
	
	@Override
	public String getName()
	{
		return "Refund";
	}
	
	@Override
	public PlayerInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.REFUND;
	}
	
	@Override
	protected void addItem(ItemInstance item)
	{
		super.addItem(item);
		try
		{
			if (getSize() > 12)
			{
				ItemInstance removedItem = _items.remove(0);
				if (removedItem != null)
				{
					ItemTable.getInstance().destroyItem("ClearRefund", removedItem, getOwner(), null);
					removedItem.updateDatabase(true);
				}
			}
		}
		catch (Exception e)
		{
			_log.error("addItem()", e);
		}
	}
	
	@Override
	public void refreshWeight()
	{
	}
	
	@Override
	public void deleteMe()
	{
		try
		{
			for (ItemInstance item : _items.values())
			{
				ItemTable.getInstance().destroyItem("ClearRefund", item, getOwner(), null);
				item.updateDatabase(true);
			}
		}
		catch (Exception e)
		{
			_log.error("deleteMe()", e);
		}
		_items.clear();
	}
	
	@Override
	public void restore()
	{
	}
}