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
package org.l2junity.gameserver.network.client.send;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.l2junity.gameserver.enums.AttributeType;
import org.l2junity.gameserver.enums.TaxType;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class SellList implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final L2MerchantInstance _lease;
	private final long _money;
	private final List<ItemInstance> _sellList;
	
	public SellList(PlayerInstance player)
	{
		this(player, null);
	}
	
	public SellList(PlayerInstance player, L2MerchantInstance lease)
	{
		_activeChar = player;
		_lease = lease;
		_money = _activeChar.getAdena();
		
		if (_lease == null)
		{
			_sellList = new LinkedList<>();
			final Summon pet = _activeChar.getPet();
			for (ItemInstance item : _activeChar.getInventory().getItems())
			{
				if (!item.isEquipped() && item.isSellable() && ((pet == null) || (item.getObjectId() != pet.getControlObjectId()))) // Pet is summoned and not the item that summoned the pet
				{
					_sellList.add(item);
				}
			}
		}
		else
		{
			_sellList = Collections.emptyList();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SELL_LIST.writeId(packet);
		
		packet.writeQ(_money);
		packet.writeD(_lease == null ? 0x00 : 1000000 + _lease.getTemplate().getId());
		packet.writeH(_sellList.size());
		
		for (ItemInstance item : _sellList)
		{
			int price = item.getItem().getReferencePrice() / 2;
			if (_lease != null)
			{
				price -= (price * _lease.getMpc().getTotalTaxRate(TaxType.SELL));
			}
			
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getDisplayId());
			packet.writeQ(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(item.isEquipped() ? 0x01 : 0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getEnchantLevel());
			packet.writeH(0x00); // TODO: Verify me
			packet.writeH(item.getCustomType2());
			packet.writeQ(item.getItem().getReferencePrice() / 2);
			// T1
			packet.writeH(item.getAttackAttributeType().getClientId());
			packet.writeH(item.getAttackAttributePower());
			for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
			{
				packet.writeH(item.getDefenceAttribute(type));
			}
			// Enchant Effects
			for (int op : item.getEnchantOptions())
			{
				packet.writeH(op);
			}
		}
		return true;
	}
}
