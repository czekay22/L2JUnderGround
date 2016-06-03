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

import java.util.HashMap;
import java.util.Map;

import org.l2junity.Config;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.data.xml.impl.BuyListData;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.buylist.Product;
import org.l2junity.gameserver.model.buylist.ProductList;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.items.Armor;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.model.items.type.ArmorType;
import org.l2junity.gameserver.model.items.type.WeaponType;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExUserInfoEquipSlot;
import org.l2junity.gameserver.network.client.send.ShopPreviewInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 ** @author Gnacik
 */
public final class RequestPreviewItem implements IClientIncomingPacket
{
	@SuppressWarnings("unused")
	private int _unk;
	private int _listId;
	private int _count;
	private int[] _items;
	
	private class RemoveWearItemsTask implements Runnable
	{
		private final PlayerInstance activeChar;
		
		protected RemoveWearItemsTask(PlayerInstance player)
		{
			activeChar = player;
		}
		
		@Override
		public void run()
		{
			try
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT2);
				activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_unk = packet.readD();
		_listId = packet.readD();
		_count = packet.readD();
		
		if (_count < 0)
		{
			_count = 0;
		}
		if (_count > 100)
		{
			return false; // prevent too long lists
		}
		
		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];
		
		// Fill _items table with all ItemID to Wear
		for (int i = 0; i < _count; i++)
		{
			_items[i] = packet.readD();
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if (_items == null)
		{
			return;
		}
		
		// Get the current player and return if null
		PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			activeChar.sendMessage("You are buying too fast.");
			return;
		}
		
		// If Alternate rule Karma punishment is set to true, forbid Wear to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (activeChar.getReputation() < 0))
		{
			return;
		}
		
		// Check current target of the player and the INTERACTION_DISTANCE
		WorldObject target = activeChar.getTarget();
		if (!activeChar.isGM() && ((target == null // No target (i.e. GM Shop)
			) || !((target instanceof L2MerchantInstance)) // Target not a merchant
		|| !activeChar.isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false) // Distance is too far
		))
		{
			return;
		}
		
		if ((_count < 1) || (_listId >= 4000000))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the current merchant targeted by the player
		final L2MerchantInstance merchant = (target instanceof L2MerchantInstance) ? (L2MerchantInstance) target : null;
		if (merchant == null)
		{
			_log.warn("Null merchant!");
			return;
		}
		
		final ProductList buyList = BuyListData.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}
		
		long totalPrice = 0;
		Map<Integer, Integer> itemList = new HashMap<>();
		
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];
			
			final Product product = buyList.getProductByItemId(itemId);
			if (product == null)
			{
				Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + itemId, Config.DEFAULT_PUNISH);
				return;
			}
			
			L2Item template = product.getItem();
			if (template == null)
			{
				continue;
			}
			
			int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if (slot < 0)
			{
				continue;
			}
			
			if (template instanceof Weapon)
			{
				if (activeChar.getRace() == Race.KAMAEL)
				{
					if (template.getItemType() == WeaponType.NONE)
					{
						continue;
					}
					else if ((template.getItemType() == WeaponType.RAPIER) || (template.getItemType() == WeaponType.CROSSBOW) || (template.getItemType() == WeaponType.ANCIENTSWORD))
					{
						continue;
					}
				}
			}
			else if (template instanceof Armor)
			{
				if (activeChar.getRace() == Race.KAMAEL)
				{
					if ((template.getItemType() == ArmorType.HEAVY) || (template.getItemType() == ArmorType.MAGIC))
					{
						continue;
					}
				}
			}
			
			if (itemList.containsKey(slot))
			{
				activeChar.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
				return;
			}
			
			itemList.put(slot, itemId);
			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > Inventory.MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to purchase over " + Inventory.MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if ((totalPrice < 0) || !activeChar.reduceAdena("Wear", totalPrice, activeChar.getLastFolkNPC(), true))
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		if (!itemList.isEmpty())
		{
			activeChar.sendPacket(new ShopPreviewInfo(itemList));
			// Schedule task
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
		}
	}
	
}
