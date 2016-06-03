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
package org.l2junity.gameserver.network.client.recv.crystalization;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.ItemCrystalizationData;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.CrystalizationData;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemChanceHolder;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.items.type.CrystalType;
import org.l2junity.gameserver.model.skills.CommonSkill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.crystalization.ExGetCrystalizingEstimation;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestCrystallizeEstimate implements IClientIncomingPacket
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
		final PlayerInstance activeChar = client.getActiveChar();
		if ((activeChar == null) || activeChar.isInCrystallize())
		{
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
		
		if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || activeChar.isInCrystallize())
		{
			client.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		int skillLevel = activeChar.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
		if (skillLevel <= 0)
		{
			client.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if ((item == null) || item.isShadowItem() || item.isTimeLimitedItem())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (item.isHeroItem())
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!item.getItem().isCrystallizable() || (item.getItem().getCrystalCount() <= 0) || (item.getItem().getCrystalType() == CrystalType.NONE))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			_log.warn(activeChar + ": tried to crystallize " + item.getItem());
			return;
		}
		
		if (_count > item.getCount())
		{
			_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;
		
		switch (item.getItem().getCrystalTypePlus())
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
		
		activeChar.setInCrystallize(true);
		
		// add crystals
		int crystalId = item.getItem().getCrystalItemId();
		int crystalAmount = item.getCrystalCount();
		final List<ItemChanceHolder> items = new ArrayList<>();
		items.add(new ItemChanceHolder(crystalId, 100, crystalAmount));
		
		final CrystalizationData data = ItemCrystalizationData.getInstance().getCrystalization(item.getId());
		if (data != null)
		{
			for (ItemChanceHolder holder : data.getItems())
			{
				if (holder.getId() != crystalId)
				{
					items.add(holder);
				}
			}
		}
		
		client.sendPacket(new ExGetCrystalizingEstimation(items));
	}
}
