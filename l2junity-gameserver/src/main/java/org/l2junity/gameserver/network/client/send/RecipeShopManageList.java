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

import java.util.Iterator;

import org.l2junity.gameserver.model.ManufactureItem;
import org.l2junity.gameserver.model.RecipeList;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class RecipeShopManageList implements IClientOutgoingPacket
{
	private final PlayerInstance _seller;
	private final boolean _isDwarven;
	private RecipeList[] _recipes;
	
	public RecipeShopManageList(PlayerInstance seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;
		
		if (_isDwarven && _seller.hasDwarvenCraft())
		{
			_recipes = _seller.getDwarvenRecipeBook();
		}
		else
		{
			_recipes = _seller.getCommonRecipeBook();
		}
		
		if (_seller.hasManufactureShop())
		{
			final Iterator<ManufactureItem> it = _seller.getManufactureItems().values().iterator();
			ManufactureItem item;
			while (it.hasNext())
			{
				item = it.next();
				if ((item.isDwarven() != _isDwarven) || !seller.hasRecipeList(item.getRecipeId()))
				{
					it.remove();
				}
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_SHOP_MANAGE_LIST.writeId(packet);
		
		packet.writeD(_seller.getObjectId());
		packet.writeD((int) _seller.getAdena());
		packet.writeD(_isDwarven ? 0x00 : 0x01);
		
		if (_recipes == null)
		{
			packet.writeD(0);
		}
		else
		{
			packet.writeD(_recipes.length);// number of items in recipe book
			
			for (int i = 0; i < _recipes.length; i++)
			{
				RecipeList temp = _recipes[i];
				packet.writeD(temp.getId());
				packet.writeD(i + 1);
			}
		}
		
		if (!_seller.hasManufactureShop())
		{
			packet.writeD(0x00);
		}
		else
		{
			packet.writeD(_seller.getManufactureItems().size());
			for (ManufactureItem item : _seller.getManufactureItems().values())
			{
				packet.writeD(item.getRecipeId());
				packet.writeD(0x00);
				packet.writeQ(item.getCost());
			}
		}
		return true;
	}
}
