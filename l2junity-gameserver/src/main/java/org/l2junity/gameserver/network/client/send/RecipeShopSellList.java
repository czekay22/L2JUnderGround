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

import org.l2junity.gameserver.model.ManufactureItem;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class RecipeShopSellList implements IClientOutgoingPacket
{
	private final PlayerInstance _buyer, _manufacturer;
	
	public RecipeShopSellList(PlayerInstance buyer, PlayerInstance manufacturer)
	{
		_buyer = buyer;
		_manufacturer = manufacturer;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_SHOP_SELL_LIST.writeId(packet);
		
		packet.writeD(_manufacturer.getObjectId());
		packet.writeD((int) _manufacturer.getCurrentMp());// Creator's MP
		packet.writeD(_manufacturer.getMaxMp());// Creator's MP
		packet.writeQ(_buyer.getAdena());// Buyer Adena
		if (!_manufacturer.hasManufactureShop())
		{
			packet.writeD(0x00);
		}
		else
		{
			packet.writeD(_manufacturer.getManufactureItems().size());
			for (ManufactureItem temp : _manufacturer.getManufactureItems().values())
			{
				packet.writeD(temp.getRecipeId());
				packet.writeD(0x00); // unknown
				packet.writeQ(temp.getCost());
			}
		}
		return true;
	}
}
