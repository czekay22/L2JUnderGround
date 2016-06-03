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

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.instancemanager.CursedWeaponsManager;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.7.2.4.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestDestroyItem implements IClientIncomingPacket
{
	private int _objectId;
	private long _count;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_count = packet.readQ();
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
		
		if (_count <= 0)
		{
			if (_count < 0)
			{
				Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to destroy item with oid " + _objectId + " but has count < 0!", Config.DEFAULT_PUNISH);
			}
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("destroy"))
		{
			activeChar.sendMessage("You are destroying items too fast.");
			return;
		}
		
		long count = _count;
		
		if (activeChar.isProcessingTransaction() || (activeChar.getPrivateStoreType() != PrivateStoreType.NONE))
		{
			client.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		if (activeChar.hasItemRequest())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DESTROY_OR_CRYSTALLIZE_ITEMS_WHILE_ENCHANTING_ATTRIBUTES);
			return;
		}
		
		final ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		
		// if we can't find the requested item, its actually a cheat
		if (itemToRemove == null)
		{
			// gm can destroy other player items
			if (activeChar.isGM())
			{
				final WorldObject obj = World.getInstance().findObject(_objectId);
				if (obj instanceof ItemInstance)
				{
					if (_count > ((ItemInstance) obj).getCount())
					{
						count = ((ItemInstance) obj).getCount();
					}
					activeChar.useAdminCommand("admin_delete_item " + _objectId + " " + count);
				}
				return;
			}
			
			client.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DESTROYED);
			return;
		}
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow(s -> s.getSkill().getItemConsumeId() == itemToRemove.getId()))
		{
			client.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DESTROYED);
			return;
		}
		
		int itemId = itemToRemove.getId();
		
		if ((!activeChar.canOverrideCond(PcCondOverride.DESTROY_ALL_ITEMS) && !itemToRemove.isDestroyable()) || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			if (itemToRemove.isHeroItem())
			{
				client.sendPacket(SystemMessageId.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			}
			else
			{
				client.sendPacket(SystemMessageId.THIS_ITEM_CANNOT_BE_DESTROYED);
			}
			return;
		}
		
		if (!itemToRemove.isStackable() && (count > 1))
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to destroy a non-stackable item with oid " + _objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		if (_count > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}
		
		if (itemToRemove.getItem().isPetItem())
		{
			final Summon pet = activeChar.getPet();
			if ((pet != null) && (pet.getControlObjectId() == _objectId))
			{
				pet.unSummon(activeChar);
			}
			
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?"))
			{
				statement.setInt(1, _objectId);
				statement.execute();
			}
			catch (Exception e)
			{
				_log.warn("could not delete pet objectid: ", e);
			}
		}
		if (itemToRemove.isTimeLimitedItem())
		{
			itemToRemove.endOfLife();
		}
		
		if (itemToRemove.isEquipped())
		{
			if (itemToRemove.getEnchantLevel() > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addInt(itemToRemove.getEnchantLevel());
				sm.addItemName(itemToRemove);
				client.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
				sm.addItemName(itemToRemove);
				client.sendPacket(sm);
			}
			
			ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			activeChar.sendInventoryUpdate(iu);
		}
		
		ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", itemToRemove, count, activeChar, null);
		
		if (removedItem == null)
		{
			return;
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
			{
				iu.addRemovedItem(removedItem);
			}
			else
			{
				iu.addModifiedItem(removedItem);
			}
			activeChar.sendInventoryUpdate(iu);
		}
		else
		{
			activeChar.sendItemList(true);
		}
	}
}
