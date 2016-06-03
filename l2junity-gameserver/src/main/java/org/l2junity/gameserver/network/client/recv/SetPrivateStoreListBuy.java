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

import static org.l2junity.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import org.l2junity.Config;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.TradeList;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.PrivateStoreManageListBuy;
import org.l2junity.gameserver.network.client.send.PrivateStoreMsgBuy;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $ CPU Disasm Packets: ddhhQQ cddb
 */
public final class SetPrivateStoreListBuy implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 44; // length of the one item
	
	private Item[] _items = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		int count = packet.readD();
		if ((count < 1) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != packet.getReadableBytes()))
		{
			return false;
		}
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = packet.readD();
			
			packet.readH(); // TODO analyse this
			packet.readH(); // TODO analyse this
			
			long cnt = packet.readQ();
			long price = packet.readQ();
			
			if ((itemId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return false;
			}
			packet.readD(); // Unk
			packet.readD(); // Unk
			packet.readD(); // Unk
			packet.readD(); // Unk
			
			_items[i] = new Item(itemId, cnt, price);
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_items == null)
		{
			player.setPrivateStoreType(PrivateStoreType.NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		long totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}
			
			totalCost += i.getCost();
			if (totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		// Check for available funds
		if (totalCost > player.getAdena())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			return;
		}
		
		player.sitDown();
		player.setPrivateStoreType(PrivateStoreType.BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	private static class Item
	{
		private final int _itemId;
		private final long _count;
		private final long _price;
		
		public Item(int id, long num, long pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((MAX_ADENA / _count) < _price)
			{
				return false;
			}
			
			list.addItemByItemId(_itemId, _count, _price);
			return true;
		}
		
		public long getCost()
		{
			return _count * _price;
		}
	}
}
