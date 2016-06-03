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

import org.l2junity.gameserver.handler.IItemHandler;
import org.l2junity.gameserver.handler.ItemHandler;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.PetItemList;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

public final class RequestPetUseItem implements IClientIncomingPacket
{
	private int _objectId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		// TODO: implement me properly
		// packet.readQ();
		// packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if ((activeChar == null) || !activeChar.hasPet())
		{
			return;
		}
		
		if (!client.getFloodProtectors().getUseItem().tryPerformAction("pet use item"))
		{
			return;
		}
		
		final L2PetInstance pet = activeChar.getPet();
		final ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (!item.getItem().isForNpc())
		{
			activeChar.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
			return;
		}
		
		if (activeChar.isAlikeDead() || pet.isDead())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		final int reuseDelay = item.getReuseDelay();
		if (reuseDelay > 0)
		{
			final long reuse = pet.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				return;
			}
		}
		
		if (!item.isEquipped() && !item.getItem().checkCondition(pet, pet, true))
		{
			return;
		}
		
		useItem(pet, item, activeChar);
	}
	
	private void useItem(L2PetInstance pet, ItemInstance item, PlayerInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (!item.getItem().isConditionAttached())
			{
				activeChar.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
				return;
			}
			
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}
			
			activeChar.sendPacket(new PetItemList(pet.getInventory().getItems()));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
			{
				if (handler.useItem(pet, item, false))
				{
					final int reuseDelay = item.getReuseDelay();
					if (reuseDelay > 0)
					{
						activeChar.addTimeStampItem(item, reuseDelay);
					}
					pet.updateAndBroadcastStatus(1);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
				_log.warn("No item handler registered for itemId: " + item.getId());
			}
		}
	}
	
}
