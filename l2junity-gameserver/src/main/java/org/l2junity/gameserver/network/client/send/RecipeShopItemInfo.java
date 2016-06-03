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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class RecipeShopItemInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	private final int _recipeId;
	
	public RecipeShopItemInfo(PlayerInstance player, int recipeId)
	{
		_player = player;
		_recipeId = recipeId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_SHOP_ITEM_INFO.writeId(packet);
		
		packet.writeD(_player.getObjectId());
		packet.writeD(_recipeId);
		packet.writeD((int) _player.getCurrentMp());
		packet.writeD(_player.getMaxMp());
		packet.writeD(0xffffffff);
		packet.writeQ(0x00);
		packet.writeC(0x00); // Trigger offering window if 1
		packet.writeQ(0x00);
		return true;
	}
}
