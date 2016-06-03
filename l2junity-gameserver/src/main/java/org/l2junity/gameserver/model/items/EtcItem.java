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
package org.l2junity.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.ExtractableProduct;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.items.type.EtcItemType;

/**
 * This class is dedicated to the management of EtcItem.
 */
public final class EtcItem extends L2Item
{
	private String _handler;
	private EtcItemType _type;
	private List<ExtractableProduct> _extractableItems;
	private boolean _isInfinite;
	
	/**
	 * Constructor for EtcItem.
	 * @param set StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public EtcItem(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatsSet set)
	{
		super.set(set);
		_type = set.getEnum("etcitem_type", EtcItemType.class, EtcItemType.NONE);
		_type1 = L2Item.TYPE1_ITEM_QUESTITEM_ADENA;
		_type2 = L2Item.TYPE2_OTHER; // default is other
		
		if (isQuestItem())
		{
			_type2 = L2Item.TYPE2_QUEST;
		}
		else if ((getId() == Inventory.ADENA_ID) || (getId() == Inventory.ANCIENT_ADENA_ID))
		{
			_type2 = L2Item.TYPE2_MONEY;
		}
		
		_handler = set.getString("handler", null); // ! null !
		_isInfinite = set.getBoolean("is_infinite", false);
	}
	
	/**
	 * @return the type of Etc Item.
	 */
	@Override
	public EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return the handler name, null if no handler for item.
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	/**
	 * @return the extractable items list.
	 */
	public List<ExtractableProduct> getExtractableItems()
	{
		return _extractableItems;
	}
	
	/**
	 * @return true if item is infinite
	 */
	public boolean isInfinite()
	{
		return _isInfinite;
	}
	
	/**
	 * @param extractableProduct
	 */
	@Override
	public void addCapsuledItem(ExtractableProduct extractableProduct)
	{
		if (_extractableItems == null)
		{
			_extractableItems = new ArrayList<>();
		}
		_extractableItems.add(extractableProduct);
	}
}
