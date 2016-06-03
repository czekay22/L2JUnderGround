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
package org.l2junity.gameserver.model.actor.request;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;

/**
 * @author Sdw
 */
public class ShapeShiftingItemRequest extends AbstractRequest
{
	private ItemInstance _appearanceStone;
	private ItemInstance _appearanceExtractItem;
	
	public ShapeShiftingItemRequest(PlayerInstance activeChar, ItemInstance appearanceStone)
	{
		super(activeChar);
		_appearanceStone = appearanceStone;
	}
	
	public ItemInstance getAppearanceStone()
	{
		return _appearanceStone;
	}
	
	public void setAppearanceStone(ItemInstance appearanceStone)
	{
		_appearanceStone = appearanceStone;
	}
	
	public ItemInstance getAppearanceExtractItem()
	{
		return _appearanceExtractItem;
	}
	
	public void setAppearanceExtractItem(ItemInstance appearanceExtractItem)
	{
		_appearanceExtractItem = appearanceExtractItem;
	}
	
	@Override
	public boolean isItemRequest()
	{
		return true;
	}
	
	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}
	
	@Override
	public boolean isUsing(int objectId)
	{
		return (objectId > 0) && ((objectId == _appearanceStone.getObjectId()) || (objectId == _appearanceExtractItem.getObjectId()));
	}
}
