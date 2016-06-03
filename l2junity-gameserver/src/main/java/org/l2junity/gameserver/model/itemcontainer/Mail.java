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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.enums.ItemLocation;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;

/**
 * @author DS
 */
public class Mail extends ItemContainer
{
	private final int _ownerId;
	private int _messageId;
	
	public Mail(int objectId, int messageId)
	{
		_ownerId = objectId;
		_messageId = messageId;
	}
	
	@Override
	public String getName()
	{
		return "Mail";
	}
	
	@Override
	public PlayerInstance getOwner()
	{
		return null;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.MAIL;
	}
	
	public int getMessageId()
	{
		return _messageId;
	}
	
	public void setNewMessageId(int messageId)
	{
		_messageId = messageId;
		for (ItemInstance item : _items.values())
		{
			item.setItemLocation(getBaseLocation(), messageId);
		}
		
		updateDatabase();
	}
	
	public void returnToWh(ItemContainer wh)
	{
		for (ItemInstance item : _items.values())
		{
			if (wh == null)
			{
				item.setItemLocation(ItemLocation.WAREHOUSE);
			}
			else
			{
				transferItem("Expire", item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}
	
	@Override
	protected void addItem(ItemInstance item)
	{
		super.addItem(item);
		item.setItemLocation(getBaseLocation(), _messageId);
	}
	
	/*
	 * Allow saving of the items without owner
	 */
	@Override
	public void updateDatabase()
	{
		for (ItemInstance item : _items.values())
		{
			item.updateDatabase(true);
		}
	}
	
	@Override
	public void restore()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND loc=? AND loc_data=?"))
		{
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setInt(3, getMessageId());
			try (ResultSet inv = statement.executeQuery())
			{
				while (inv.next())
				{
					final ItemInstance item = new ItemInstance(inv);
					World.getInstance().storeObject(item);
					
					// If stackable item is found just add to current quantity
					if (item.isStackable() && (getItemByItemId(item.getId()) != null))
					{
						addItem("Restore", item, null, null);
					}
					else
					{
						addItem(item);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("could not restore container:", e);
		}
	}
	
	@Override
	public void deleteMe()
	{
		for (ItemInstance item : _items.values())
		{
			item.updateDatabase(true);
			item.deleteMe();
			World.getInstance().removeObject(item);
		}
		
		_items.clear();
	}
	
	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
}