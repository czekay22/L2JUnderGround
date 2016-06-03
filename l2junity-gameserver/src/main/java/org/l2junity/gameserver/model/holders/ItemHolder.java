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
package org.l2junity.gameserver.model.holders;

import org.l2junity.gameserver.model.interfaces.IIdentifiable;

/**
 * A simple DTO for items; contains item ID and count.<br>
 * Extended by {@link ItemChanceHolder}, {@link QuestItemHolder}, {@link UniqueItemHolder}.
 * @author UnAfraid
 */
public class ItemHolder implements IIdentifiable
{
	private final int _id;
	private final long _count;
	
	public ItemHolder(int id, long count)
	{
		_id = id;
		_count = count;
	}
	
	/**
	 * @return the ID of the item contained in this object
	 */
	@Override
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the count of items contained in this object
	 */
	public long getCount()
	{
		return _count;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ItemHolder))
		{
			return false;
		}
		else if (obj == this)
		{
			return true;
		}
		final ItemHolder objInstance = (ItemHolder) obj;
		return (_id == objInstance.getId()) && (_count == objInstance.getCount());
	}
	
	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + "] ID: " + _id + ", count: " + _count;
	}
}
