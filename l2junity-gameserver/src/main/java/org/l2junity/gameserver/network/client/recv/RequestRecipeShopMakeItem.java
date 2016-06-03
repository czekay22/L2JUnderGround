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

import org.l2junity.gameserver.RecipeController;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * @author Administrator
 */
public final class RequestRecipeShopMakeItem implements IClientIncomingPacket
{
	private int _id;
	private int _recipeId;
	@SuppressWarnings("unused")
	private long _unknown;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_id = packet.readD();
		_recipeId = packet.readD();
		_unknown = packet.readQ();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().getManufacture().tryPerformAction("RecipeShopMake"))
		{
			return;
		}
		
		PlayerInstance manufacturer = World.getInstance().getPlayer(_id);
		if (manufacturer == null)
		{
			return;
		}
		
		if (manufacturer.getInstanceWorld() != activeChar.getInstanceWorld())
		{
			return;
		}
		
		if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			activeChar.sendMessage("You cannot create items while trading.");
			return;
		}
		if (manufacturer.getPrivateStoreType() != PrivateStoreType.MANUFACTURE)
		{
			// activeChar.sendMessage("You cannot create items while trading.");
			return;
		}
		
		if (activeChar.isInCraftMode() || manufacturer.isInCraftMode())
		{
			activeChar.sendMessage("You are currently in Craft Mode.");
			return;
		}
		if (Util.checkIfInRange(150, activeChar, manufacturer, true))
		{
			RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
		}
	}
}
