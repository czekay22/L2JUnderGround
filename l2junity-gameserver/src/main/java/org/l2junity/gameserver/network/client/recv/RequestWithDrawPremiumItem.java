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

import org.l2junity.Config;
import org.l2junity.gameserver.model.PremiumItem;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExGetPremiumItemList;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * @author Gnacik
 */
public final class RequestWithDrawPremiumItem implements IClientIncomingPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemCount;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_itemNum = packet.readD();
		_charId = packet.readD();
		_itemCount = packet.readQ();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		else if (_itemCount <= 0)
		{
			return;
		}
		else if (activeChar.getObjectId() != _charId)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Incorrect owner, Player: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (activeChar.getPremiumItemList().isEmpty())
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() + " try to get item with empty list!", Config.DEFAULT_PUNISH);
			return;
		}
		else if ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(false))
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_DIMENSIONAL_ITEM_BECAUSE_YOU_HAVE_EXCEED_YOUR_INVENTORY_WEIGHT_QUANTITY_LIMIT);
			return;
		}
		else if (activeChar.isProcessingTransaction())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_DIMENSIONAL_ITEM_DURING_AN_EXCHANGE);
			return;
		}
		
		PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);
		if (_item == null)
		{
			return;
		}
		else if (_item.getCount() < _itemCount)
		{
			return;
		}
		
		long itemsLeft = (_item.getCount() - _itemCount);
		
		activeChar.addItem("PremiumItem", _item.getItemId(), _itemCount, activeChar.getTarget(), true);
		
		if (itemsLeft > 0)
		{
			_item.updateCount(itemsLeft);
			activeChar.updatePremiumItem(_itemNum, itemsLeft);
		}
		else
		{
			activeChar.getPremiumItemList().remove(_itemNum);
			activeChar.deletePremiumItem(_itemNum);
		}
		
		if (activeChar.getPremiumItemList().isEmpty())
		{
			client.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_DIMENSIONAL_ITEMS_TO_BE_FOUND);
		}
		else
		{
			client.sendPacket(new ExGetPremiumItemList(activeChar));
		}
	}
}
