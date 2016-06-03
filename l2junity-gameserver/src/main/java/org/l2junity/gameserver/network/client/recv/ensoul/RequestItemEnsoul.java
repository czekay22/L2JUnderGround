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
package org.l2junity.gameserver.network.client.recv.ensoul;

import org.l2junity.gameserver.data.xml.impl.EnsoulData;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ensoul.EnsoulOption;
import org.l2junity.gameserver.model.ensoul.EnsoulStone;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.ensoul.ExEnsoulResult;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class RequestItemEnsoul implements IClientIncomingPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(IClientIncomingPacket.class);
	private int _itemObjectId;
	private EnsoulItemOption[] _options;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
		int options = packet.readC();
		if ((options > 0) && (options <= 3))
		{
			_options = new EnsoulItemOption[options];
			for (int i = 0; i < options; i++)
			{
				final int type = packet.readC();
				final int position = packet.readC();
				final int soulCrystalObjectId = packet.readD();
				final int soulCrystalOption = packet.readD();
				if ((position > 0) && (position <= 3) && ((type == 1) || (type == 2)))
				{
					_options[i] = new EnsoulItemOption(type, position, soulCrystalObjectId, soulCrystalOption);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_USING_THE_PRIVATE_STORE_WORKSHOP);
			return;
		}
		else if (player.hasAbnormalType(AbnormalType.FREEZING))
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_FROZEN);
		}
		else if (player.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_DEAD);
			return;
		}
		else if ((player.getActiveTradeList() != null) || player.hasItemRequest())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_TRADING);
			return;
		}
		else if (player.hasAbnormalType(AbnormalType.PARALYZE))
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_PETRIFIED);
			return;
		}
		else if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_FISHING);
			return;
		}
		else if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_SEATED);
			return;
		}
		else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_THE_SOUL_CRYSTAL_SYSTEM_WHILE_IN_BATTLE);
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_itemObjectId);
		if (item == null)
		{
			LOGGER.warn("Player: {} attempting to ensoul item without having it!", player);
			return;
		}
		else if (!item.isEquipable())
		{
			LOGGER.warn("Player: {} attempting to ensoul non equippable item: {}!", player, item);
			return;
		}
		else if (!item.isWeapon())
		{
			LOGGER.warn("Player: {} attempting to ensoul item that's not a weapon: {}!", player, item);
			return;
		}
		else if (item.isCommonItem())
		{
			LOGGER.warn("Player: {} attempting to ensoul common item: {}!", player, item);
			return;
		}
		else if (item.isShadowItem())
		{
			LOGGER.warn("Player: {} attempting to ensoul shadow item: {}!", player, item);
			return;
		}
		else if (item.isHeroItem())
		{
			LOGGER.warn("Player: {} attempting to ensoul hero item: {}!", player, item);
			return;
		}
		
		if ((_options == null) || (_options.length == 0))
		{
			LOGGER.warn("Player: {} attempting to ensoul item without any special ability declared!", player);
			return;
		}
		
		int success = 0;
		final InventoryUpdate iu = new InventoryUpdate();
		for (EnsoulItemOption itemOption : _options)
		{
			final int position = itemOption.getPosition() - 1;
			final ItemInstance soulCrystal = player.getInventory().getItemByObjectId(itemOption.getSoulCrystalObjectId());
			if (soulCrystal == null)
			{
				player.sendPacket(SystemMessageId.INVALID_SOUL_CRYSTAL);
				continue;
			}
			
			final EnsoulStone stone = EnsoulData.getInstance().getStone(soulCrystal.getId());
			if (stone == null)
			{
				continue;
			}
			
			if (!stone.getOptions().contains(itemOption.getSoulCrystalOption()))
			{
				LOGGER.warn("Player: {} attempting to ensoul item option that stone doesn't contains!", player);
				continue;
			}
			
			final EnsoulOption option = EnsoulData.getInstance().getOption(itemOption.getSoulCrystalOption());
			if (option == null)
			{
				LOGGER.warn("Player: {} attempting to ensoul item option that doesn't exists!", player);
				continue;
			}
			
			final ItemHolder fee;
			if (itemOption.getType() == 1)
			{
				fee = EnsoulData.getInstance().getEnsoulFee(item.getItem().getCrystalType(), position);
				if ((itemOption.getPosition() == 1) || (itemOption.getPosition() == 2))
				{
					if (item.getSpecialAbility(position) != null)
					{
						LOGGER.warn("Player: {} attempting to ensoul item option add but he's actually trying to replace!", player);
						continue;
					}
				}
				else if (itemOption.getPosition() == 3)
				{
					if (item.getAdditionalSpecialAbility(position) != null)
					{
						LOGGER.warn("Player: {} attempting to ensoul special item option add but he's actually trying to replace!", player);
						continue;
					}
				}
			}
			else if (itemOption.getType() == 2)
			{
				fee = EnsoulData.getInstance().getResoulFee(item.getItem().getCrystalType(), position);
				if ((itemOption.getPosition() == 1) || (itemOption.getPosition() == 2))
				{
					if (item.getSpecialAbility(position) == null)
					{
						LOGGER.warn("Player: {} attempting to ensoul item option replace but he's actually trying to add!", player);
						continue;
					}
				}
				else if (itemOption.getPosition() == 3)
				{
					if (item.getAdditionalSpecialAbility(position) == null)
					{
						LOGGER.warn("Player: {} attempting to ensoul special item option replace but he's actually trying to add!", player);
						continue;
					}
				}
			}
			else
			{
				LOGGER.warn("Player: {} attempting to ensoul item option with unhandled type: {}!", player, itemOption.getType());
				continue;
			}
			
			if (fee == null)
			{
				LOGGER.warn("Player: {} attempting to ensoul item option that doesn't exists!", player);
				continue;
			}
			
			final ItemInstance gemStones = player.getInventory().getItemByItemId(fee.getId());
			if ((gemStones == null) || (gemStones.getCount() < fee.getCount()))
			{
				continue;
			}
			
			if (player.destroyItem("EnsoulOption", soulCrystal, 1, player, true) && player.destroyItem("EnsoulOption", gemStones, fee.getCount(), player, true))
			{
				item.addSpecialAbility(option, position, stone.getSlotType(), true);
				success = 1;
			}
			
			iu.addModifiedItem(soulCrystal);
			iu.addModifiedItem(gemStones);
			iu.addModifiedItem(item);
		}
		player.sendInventoryUpdate(iu);
		if (item.isEquipped())
		{
			item.applySpecialAbilities();
		}
		player.sendPacket(new ExEnsoulResult(success, item));
	}
	
	static class EnsoulItemOption
	{
		private final int _type;
		private final int _position;
		private final int _soulCrystalObjectId;
		private final int _soulCrystalOption;
		
		EnsoulItemOption(int type, int position, int soulCrystalObjectId, int soulCrystalOption)
		{
			_type = type;
			_position = position;
			_soulCrystalObjectId = soulCrystalObjectId;
			_soulCrystalOption = soulCrystalOption;
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getPosition()
		{
			return _position;
		}
		
		public int getSoulCrystalObjectId()
		{
			return _soulCrystalObjectId;
		}
		
		public int getSoulCrystalOption()
		{
			return _soulCrystalOption;
		}
	}
}
