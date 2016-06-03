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

import org.l2junity.gameserver.data.xml.impl.RecipeData;
import org.l2junity.gameserver.model.RecipeList;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class RecipeItemMakeInfo implements IClientOutgoingPacket
{
	private final int _id;
	private final PlayerInstance _activeChar;
	private final boolean _success;
	
	public RecipeItemMakeInfo(int id, PlayerInstance player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}
	
	public RecipeItemMakeInfo(int id, PlayerInstance player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		final RecipeList recipe = RecipeData.getInstance().getRecipeList(_id);
		if (recipe != null)
		{
			OutgoingPackets.RECIPE_ITEM_MAKE_INFO.writeId(packet);
			packet.writeD(_id);
			packet.writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
			packet.writeD((int) _activeChar.getCurrentMp());
			packet.writeD(_activeChar.getMaxMp());
			packet.writeD(_success ? 1 : 0); // item creation success/failed
			packet.writeC(0x00);
			packet.writeQ(0x00);
			return true;
		}
		_log.info("Character: " + _activeChar + ": Requested unexisting recipe with id = " + _id);
		return false;
	}
}
