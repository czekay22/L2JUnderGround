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

import java.util.Arrays;

import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.EtcItem;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author Zoey76
 */
public class RequestUnEquipItem implements IClientIncomingPacket
{
	private int _slot;
	
	/**
	 * Packet type id 0x16 format: cd
	 */
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_slot = packet.readD();
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
		
		final ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		// Wear-items are not to be unequipped.
		if (item == null)
		{
			return;
		}
		
		// The English system message say weapon, but it's applied to any equipped item.
		if (activeChar.isAttackingNow() || activeChar.isCastingNow())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_WEAPONS_DURING_AN_ATTACK);
			return;
		}
		
		// Arrows and bolts.
		if ((_slot == L2Item.SLOT_L_HAND) && (item.getItem() instanceof EtcItem))
		{
			return;
		}
		
		// Prevent of unequipping a cursed weapon.
		if ((_slot == L2Item.SLOT_LR_HAND) && (activeChar.isCursedWeaponEquipped() || activeChar.isCombatFlagEquipped()))
		{
			return;
		}
		
		// Prevent player from unequipping items in special conditions.
		if (activeChar.hasBlockActions() || activeChar.isAlikeDead())
		{
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
		{
			client.sendPacket(SystemMessageId.THAT_ITEM_CANNOT_BE_TAKEN_OFF);
			return;
		}
		
		if (item.isWeapon() && item.getWeaponItem().isForceEquip() && !activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS))
		{
			client.sendPacket(SystemMessageId.THAT_ITEM_CANNOT_BE_TAKEN_OFF);
			return;
		}
		
		final ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		activeChar.broadcastUserInfo();
		
		// This can be 0 if the user pressed the right mouse button twice very fast.
		if (unequipped.length > 0)
		{
			SystemMessage sm = null;
			if (unequipped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addInt(unequipped[0].getEnchantLevel());
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
			}
			sm.addItemName(unequipped[0]);
			client.sendPacket(sm);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItems(Arrays.asList(unequipped));
			activeChar.sendInventoryUpdate(iu);
		}
	}
}
