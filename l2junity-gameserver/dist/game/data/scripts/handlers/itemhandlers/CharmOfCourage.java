/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import org.l2junity.gameserver.handler.IItemHandler;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.send.EtcStatusUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Charm Of Courage Handler
 * @author Zealar
 */
public class CharmOfCourage implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		
		if (!playable.isPlayer())
		{
			return false;
		}
		
		final PlayerInstance activeChar = playable.getActingPlayer();
		
		int level = activeChar.getLevel();
		final int itemLevel = item.getItem().getCrystalType().getId();
		
		if (level < 20)
		{
			level = 0;
		}
		else if (level < 40)
		{
			level = 1;
		}
		else if (level < 52)
		{
			level = 2;
		}
		else if (level < 61)
		{
			level = 3;
		}
		else if (level < 76)
		{
			level = 4;
		}
		else
		{
			level = 5;
		}
		
		if (itemLevel < level)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(item.getId());
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			activeChar.setCharmOfCourage(true);
			activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			return true;
		}
		return false;
	}
}
