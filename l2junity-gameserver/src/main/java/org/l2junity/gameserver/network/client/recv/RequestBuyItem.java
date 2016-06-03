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

import static org.l2junity.gameserver.model.actor.Npc.INTERACTION_DISTANCE;
import static org.l2junity.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.BuyListData;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.buylist.Product;
import org.l2junity.gameserver.model.buylist.ProductList;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExBuySellList;
import org.l2junity.gameserver.network.client.send.ExUserInfoInvenWeight;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

public final class RequestBuyItem implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 12;
	private int _listId;
	private List<ItemHolder> _items = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_listId = packet.readD();
		int size = packet.readD();
		if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != packet.getReadableBytes()))
		{
			return false;
		}
		
		_items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			int itemId = packet.readD();
			long count = packet.readQ();
			if ((itemId < 1) || (count < 1))
			{
				_items = null;
				return false;
			}
			_items.add(new ItemHolder(itemId, count));
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You are buying too fast.");
			return;
		}
		
		if (_items == null)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getReputation() < 0))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		WorldObject target = player.getTarget();
		Creature merchant = null;
		if (!player.isGM())
		{
			if (!(target instanceof L2MerchantInstance) || (!player.isInsideRadius(target, INTERACTION_DISTANCE, true, false)) || (player.getInstanceWorld() != target.getInstanceWorld()))
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			merchant = (Creature) target;
		}
		
		double castleTaxRate = 0;
		double baseTaxRate = 0;
		
		if ((merchant == null) && !player.isGM())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ProductList buyList = BuyListData.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (merchant != null)
		{
			if (!buyList.isNpcAllowed(merchant.getId()))
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (merchant instanceof L2MerchantInstance)
			{
				castleTaxRate = ((L2MerchantInstance) merchant).getMpc().getCastleTaxRate();
				baseTaxRate = ((L2MerchantInstance) merchant).getMpc().getBaseTaxRate();
			}
			else
			{
				baseTaxRate = 0.5;
			}
		}
		
		long subTotal = 0;
		
		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (ItemHolder i : _items)
		{
			long price = -1;
			
			final Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + i.getId(), Config.DEFAULT_PUNISH);
				return;
			}
			
			if (!product.getItem().isStackable() && (i.getCount() > 1))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			price = product.getPrice();
			if ((product.getItemId() >= 3960) && (product.getItemId() <= 4026))
			{
				price *= Config.RATE_SIEGE_GUARDS_PRICE;
			}
			
			if (price < 0)
			{
				_log.warn("ERROR, no price found .. wrong buylist ??");
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if ((price == 0) && !player.isGM() && Config.ONLY_GM_ITEMS_FREE)
			{
				player.sendMessage("Ohh Cheat dont work? You have a problem now!");
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (product.hasLimitedStock())
			{
				// trying to buy more then available
				if (i.getCount() > product.getCount())
				{
					client.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			if ((MAX_ADENA / i.getCount()) < price)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			// first calculate price per item with tax, then multiply by count
			price = (long) (price * (1 + castleTaxRate + baseTaxRate));
			subTotal += i.getCount() * price;
			if (subTotal > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			
			weight += i.getCount() * product.getItem().getWeight();
			if (player.getInventory().getItemByItemId(product.getItemId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.isGM() && ((weight > Integer.MAX_VALUE) || (weight < 0) || !player.getInventory().validateWeight((int) weight)))
		{
			client.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!player.isGM() && ((slots > Integer.MAX_VALUE) || (slots < 0) || !player.getInventory().validateCapacity((int) slots)))
		{
			client.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if ((subTotal < 0) || !player.reduceAdena("Buy", subTotal, player.getLastFolkNPC(), false))
		{
			client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Proceed the purchase
		for (ItemHolder i : _items)
		{
			Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + i.getId(), Config.DEFAULT_PUNISH);
				continue;
			}
			
			if (product.hasLimitedStock())
			{
				if (product.decreaseCount(i.getCount()))
				{
					player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
				}
			}
			else
			{
				player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
			}
		}
		
		// add to castle treasury
		if (merchant instanceof L2MerchantInstance)
		{
			((L2MerchantInstance) merchant).getCastle().addToTreasury((long) (subTotal * castleTaxRate));
		}
		
		client.sendPacket(new ExUserInfoInvenWeight(player));
		client.sendPacket(new ExBuySellList(player, true));
	}
}
