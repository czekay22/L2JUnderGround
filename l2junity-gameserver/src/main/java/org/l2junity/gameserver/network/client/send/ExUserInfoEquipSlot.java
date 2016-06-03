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

import org.l2junity.gameserver.enums.InventorySlot;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.PcInventory;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final PlayerInstance _activeChar;
	
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	public ExUserInfoEquipSlot(PlayerInstance cha)
	{
		this(cha, true);
	}
	
	public ExUserInfoEquipSlot(PlayerInstance cha, boolean addAll)
	{
		_activeChar = cha;
		
		if (addAll)
		{
			addComponentType(InventorySlot.values());
		}
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_EQUIP_SLOT.writeId(packet);
		
		packet.writeD(_activeChar.getObjectId());
		packet.writeH(InventorySlot.values().length);
		packet.writeB(_masks);
		
		final PcInventory inventory = _activeChar.getInventory();
		for (InventorySlot slot : InventorySlot.values())
		{
			if (containsMask(slot))
			{
				packet.writeH(22); // 10 + 4 * 3
				packet.writeD(inventory.getPaperdollObjectId(slot.getSlot()));
				packet.writeD(inventory.getPaperdollItemId(slot.getSlot()));
				packet.writeQ(inventory.getPaperdollAugmentationId(slot.getSlot()));
				packet.writeD(inventory.getPaperdollItemVisualId(slot.getSlot()));
			}
		}
		return true;
	}
}