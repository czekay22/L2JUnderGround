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

import java.util.Collection;

import org.l2junity.Config;
import org.l2junity.gameserver.instancemanager.MentorManager;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class TradeStart extends AbstractItemPacket
{
	private final PlayerInstance _activeChar;
	private final PlayerInstance _partner;
	private final Collection<ItemInstance> _itemList;
	private int _mask = 0;
	
	public TradeStart(PlayerInstance player)
	{
		_activeChar = player;
		_partner = player.getActiveTradeList().getPartner();
		_itemList = _activeChar.getInventory().getAvailableItems(true, (_activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS), false);
		
		if (_partner != null)
		{
			if (player.getFriendList().contains(_partner.getObjectId()))
			{
				_mask |= 0x01;
			}
			if ((player.getClanId() > 0) && (_partner.getClanId() == _partner.getClanId()))
			{
				_mask |= 0x02;
			}
			if ((MentorManager.getInstance().getMentee(player.getObjectId(), _partner.getObjectId()) != null) || (MentorManager.getInstance().getMentee(_partner.getObjectId(), player.getObjectId()) != null))
			{
				_mask |= 0x04;
			}
			if ((player.getAllyId() > 0) && (player.getAllyId() == _partner.getAllyId()))
			{
				_mask |= 0x08;
			}
			
			// Does not shows level
			if (_partner.isGM())
			{
				_mask |= 0x10;
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if ((_activeChar.getActiveTradeList() == null) || (_partner == null))
		{
			return false;
		}
		
		OutgoingPackets.TRADE_START.writeId(packet);
		packet.writeD(_partner.getObjectId());
		packet.writeC(_mask); // some kind of mask
		if ((_mask & 0x10) == 0)
		{
			packet.writeC(_partner.getLevel());
		}
		packet.writeH(_itemList.size());
		for (ItemInstance item : _itemList)
		{
			writeItem(packet, item);
		}
		return true;
	}
}
