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
package handlers.effecthandlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Disarm by inventory slot effect implementation. At end of effect, it re-equips that item.
 * @author Nik
 */
public final class Disarmor extends AbstractEffect
{
	private final Map<Integer, Integer> _unequippedItems; // PlayerObjId, ItemObjId
	private final int _slot;
	
	public Disarmor(StatsSet params)
	{
		_unequippedItems = new ConcurrentHashMap<>();
		
		String slot = params.getString("slot", "chest");
		_slot = ItemTable._slots.getOrDefault(slot, L2Item.SLOT_NONE);
		if (_slot == L2Item.SLOT_NONE)
		{
			_log.error("Unknown bodypart slot for effect: {}", slot);
		}
		
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (_slot != L2Item.SLOT_NONE) && info.getEffected().isPlayer();
	}
	
	@Override
	public void continuousInstant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (!effected.isPlayer())
		{
			return;
		}
		
		PlayerInstance player = effected.getActingPlayer();
		ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		if (unequiped.length > 0)
		{
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendInventoryUpdate(iu);
			player.broadcastUserInfo();
			
			SystemMessage sm = null;
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addInt(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0]);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
				sm.addItemName(unequiped[0]);
			}
			player.sendPacket(sm);
			effected.getInventory().blockItemSlot(_slot);
			_unequippedItems.put(effected.getObjectId(), unequiped[0].getObjectId());
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.getEffected().isPlayer())
		{
			return;
		}
		
		Integer disarmedObjId = _unequippedItems.remove(info.getEffected().getObjectId());
		if ((disarmedObjId != null) && (disarmedObjId > 0))
		{
			PlayerInstance player = info.getEffected().getActingPlayer();
			info.getEffected().getInventory().unblockItemSlot(_slot);
			
			ItemInstance item = player.getInventory().getItemByObjectId(disarmedObjId);
			if (item != null)
			{
				player.getInventory().equipItem(item);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				player.sendInventoryUpdate(iu);
				
				SystemMessage sm = null;
				if (item.isEquipped())
				{
					if (item.getEnchantLevel() > 0)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPPED_S1_S2);
						sm.addInt(item.getEnchantLevel());
						sm.addItemName(item);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EQUIPPED_YOUR_S1);
						sm.addItemName(item);
					}
					player.sendPacket(sm);
				}
			}
		}
		super.onExit(info);
	}
}
