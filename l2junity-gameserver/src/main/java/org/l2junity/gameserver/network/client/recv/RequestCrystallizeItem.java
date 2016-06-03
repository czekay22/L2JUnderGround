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

import java.util.ArrayList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.data.xml.impl.ItemCrystalizationData;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.CrystalizationData;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemChanceHolder;
import org.l2junity.gameserver.model.itemcontainer.PcInventory;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.items.type.CrystalType;
import org.l2junity.gameserver.model.skills.CommonSkill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestCrystallizeItem implements IClientIncomingPacket
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
			_log.debug("RequestCrystalizeItem: activeChar was null");
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("crystallize"))
		{
			activeChar.sendMessage("You are crystallizing too fast.");
			return;
		}
		
		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		
		if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || !activeChar.isInCrystallize())
		{
			client.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		int skillLevel = activeChar.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
		if (skillLevel <= 0)
		{
			client.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			if ((activeChar.getRace() != Race.DWARF) && (activeChar.getClassId().ordinal() != 117) && (activeChar.getClassId().ordinal() != 55))
			{
				_log.info("Player " + activeChar + " used crystalize with classid: " + activeChar.getClassId().ordinal());
			}
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			if (item == null)
			{
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (item.isHeroItem())
			{
				return;
			}
			
			if (_count > item.getCount())
			{
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}
		
		final ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if ((itemToRemove == null) || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem())
		{
			return;
		}
		
		if (!itemToRemove.getItem().isCrystallizable() || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == CrystalType.NONE))
		{
			_log.warn(activeChar.getName() + " (" + activeChar.getObjectId() + ") tried to crystallize " + itemToRemove.getItem().getId());
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;
		
		switch (itemToRemove.getItem().getCrystalTypePlus())
		{
			case D:
			{
				if (skillLevel < 1)
				{
					canCrystallize = false;
				}
				break;
			}
			case C:
			{
				if (skillLevel < 2)
				{
					canCrystallize = false;
				}
				break;
			}
			case B:
			{
				if (skillLevel < 3)
				{
					canCrystallize = false;
				}
				break;
			}
			case A:
			{
				if (skillLevel < 4)
				{
					canCrystallize = false;
				}
				break;
			}
			case S:
			{
				if (skillLevel < 5)
				{
					canCrystallize = false;
				}
				break;
			}
			case R:
			{
				if (skillLevel < 6)
				{
					canCrystallize = false;
				}
				break;
			}
		}
		
		if (!canCrystallize)
		{
			client.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// activeChar.setInCrystallize(true);
		
		// unequip if needed
		SystemMessage sm;
		if (itemToRemove.isEquipped())
		{
			ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance item : unequiped)
			{
				iu.addModifiedItem(item);
			}
			activeChar.sendInventoryUpdate(iu);
			
			if (itemToRemove.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addInt(itemToRemove.getEnchantLevel());
				sm.addItemName(itemToRemove);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
				sm.addItemName(itemToRemove);
			}
			client.sendPacket(sm);
		}
		
		// remove from inventory
		final ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		activeChar.sendInventoryUpdate(iu);
		
		final int crystalId = itemToRemove.getItem().getCrystalItemId();
		final int crystalAmount = itemToRemove.getCrystalCount();
		
		final List<ItemChanceHolder> items = new ArrayList<>();
		items.add(new ItemChanceHolder(crystalId, 100, crystalAmount));
		
		final CrystalizationData data = ItemCrystalizationData.getInstance().getCrystalization(itemToRemove.getId());
		if (data != null)
		{
			data.getItems().stream().filter(holder -> (holder.getId() != crystalId)).forEach(items::add);
		}
		
		for (ItemChanceHolder holder : items)
		{
			final double rand = Rnd.nextDouble() * 100;
			if (rand < holder.getChance())
			{
				// add crystals
				final ItemInstance createdItem = activeChar.getInventory().addItem("Crystalize", holder.getId(), holder.getCount(), activeChar, activeChar);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(createdItem);
				sm.addLong(holder.getCount());
				client.sendPacket(sm);
			}
		}
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_CRYSTALLIZED);
		sm.addItemName(removedItem);
		client.sendPacket(sm);
		
		activeChar.broadcastUserInfo();
		
		World.getInstance().removeObject(removedItem);
		
		activeChar.setInCrystallize(false);
	}
}
