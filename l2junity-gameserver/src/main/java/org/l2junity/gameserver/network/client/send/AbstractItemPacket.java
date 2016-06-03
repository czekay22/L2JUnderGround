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

import org.l2junity.gameserver.enums.ItemListType;
import org.l2junity.gameserver.model.ItemInfo;
import org.l2junity.gameserver.model.TradeItem;
import org.l2junity.gameserver.model.buylist.Product;
import org.l2junity.gameserver.model.ensoul.EnsoulOption;
import org.l2junity.gameserver.model.itemcontainer.PcInventory;
import org.l2junity.gameserver.model.items.WarehouseItem;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemPacket extends AbstractMaskPacket<ItemListType>
{
	private static final byte[] MASKS =
	{
		0x00
	};
	
	@Override
	protected byte[] getMasks()
	{
		return MASKS;
	}
	
	protected void writeItem(PacketWriter packet, TradeItem item)
	{
		writeItem(packet, new ItemInfo(item));
	}
	
	protected void writeItem(PacketWriter packet, WarehouseItem item)
	{
		writeItem(packet, new ItemInfo(item));
	}
	
	protected void writeItem(PacketWriter packet, ItemInstance item)
	{
		writeItem(packet, new ItemInfo(item));
	}
	
	protected void writeItem(PacketWriter packet, Product item)
	{
		writeItem(packet, new ItemInfo(item));
	}
	
	protected void writeTradeItem(PacketWriter packet, TradeItem item)
	{
		packet.writeH(item.getItem().getType1());
		packet.writeD(item.getObjectId()); // ObjectId
		packet.writeD(item.getItem().getDisplayId()); // ItemId
		packet.writeQ(item.getCount()); // Quantity
		packet.writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		packet.writeC(item.getCustomType1()); // Filler (always 0)
		packet.writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		packet.writeC(item.getEnchant()); // Enchant level (pet level shown in control item)
		packet.writeC(0x00); // TODO : Find me
		packet.writeH(0x00); // Equipped : 00-No, 01-yes
		packet.writeH(item.getCustomType2());
		writeItemElementalAndEnchant(packet, new ItemInfo(item));
	}
	
	protected void writeItem(PacketWriter packet, ItemInfo item)
	{
		final int mask = calculateMask(item);
		// cddcQcchQccddc
		packet.writeC(mask);
		packet.writeD(item.getObjectId()); // ObjectId
		packet.writeD(item.getItem().getDisplayId()); // ItemId
		packet.writeC(item.getItem().isQuestItem() || (item.getEquipped() == 1) ? 0xFF : item.getLocation()); // T1
		packet.writeQ(item.getCount()); // Quantity
		packet.writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		packet.writeC(item.getCustomType1()); // Filler (always 0)
		packet.writeH(item.getEquipped()); // Equipped : 00-No, 01-yes
		packet.writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		packet.writeC(item.getEnchant()); // Enchant level (pet level shown in control item)
		packet.writeC(0x01); // TODO : Find me
		packet.writeD(item.getMana());
		packet.writeD(item.getTime());
		packet.writeC(0x01); // GOD Item enabled = 1 disabled (red) = 0
		if (containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			packet.writeQ(item.getAugmentationBonus());
		}
		if (containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			writeItemElemental(packet, item);
		}
		if (containsMask(mask, ItemListType.ENCHANT_EFFECT))
		{
			writeItemEnchantEffect(packet, item);
		}
		if (containsMask(mask, ItemListType.VISUAL_ID))
		{
			packet.writeD(item.getVisualId()); // Item remodel visual ID
		}
		if (containsMask(mask, ItemListType.SOUL_CRYSTAL))
		{
			packet.writeC(item.getSoulCrystalOptions().size()); // Size of regular soul crystal options.
			for (EnsoulOption option : item.getSoulCrystalOptions())
			{
				packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
			}
			
			packet.writeC(item.getSoulCrystalSpecialOptions().size()); // Size of special soul crystal options.
			for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
			{
				packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
			}
		}
	}
	
	protected static int calculateMask(ItemInfo item)
	{
		int mask = 0;
		if (item.getAugmentationBonus() > 0)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}
		
		if (item.getAttackElementType() >= 0)
		{
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}
		else
		{
			for (byte i = 0; i < 6; i++)
			{
				if (item.getElementDefAttr(i) > 0)
				{
					mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
					break;
				}
			}
		}
		
		if (item.getEnchantOptions() != null)
		{
			for (int id : item.getEnchantOptions())
			{
				if (id > 0)
				{
					mask |= ItemListType.ENCHANT_EFFECT.getMask();
					break;
				}
			}
		}
		
		if (item.getVisualId() > 0)
		{
			mask |= ItemListType.VISUAL_ID.getMask();
		}
		
		if (!item.getSoulCrystalOptions().isEmpty() || !item.getSoulCrystalSpecialOptions().isEmpty())
		{
			mask |= ItemListType.SOUL_CRYSTAL.getMask();
		}
		
		return mask;
	}
	
	protected void writeItemElementalAndEnchant(PacketWriter packet, ItemInfo item)
	{
		writeItemElemental(packet, item);
		writeItemEnchantEffect(packet, item);
	}
	
	protected void writeItemElemental(PacketWriter packet, ItemInfo item)
	{
		packet.writeH(item.getAttackElementType());
		packet.writeH(item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			packet.writeH(item.getElementDefAttr(i));
		}
	}
	
	protected void writeItemEnchantEffect(PacketWriter packet, ItemInfo item)
	{
		// Enchant Effects
		for (int op : item.getEnchantOptions())
		{
			packet.writeD(op);
		}
	}
	
	protected void writeInventoryBlock(PacketWriter packet, PcInventory inventory)
	{
		if (inventory.hasInventoryBlock())
		{
			packet.writeH(inventory.getBlockItems().size());
			packet.writeC(inventory.getBlockMode().getClientId());
			for (int id : inventory.getBlockItems())
			{
				packet.writeD(id);
			}
		}
		else
		{
			packet.writeH(0x00);
		}
	}
	
	protected void writeCommissionItem(PacketWriter packet, ItemInfo item)
	{
		packet.writeD(0); // Always 0
		packet.writeD(item.getItem().getId());
		packet.writeQ(item.getCount());
		packet.writeH(item.getItem().getType2());
		packet.writeQ(item.getItem().getBodyPart());
		packet.writeC(item.getEnchant());
		packet.writeC(0x00); // TODO: Find me
		packet.writeH(item.getCustomType2());
		writeItemElementalAndEnchant(packet, item);
		packet.writeD(item.getVisualId());
		packet.writeC(item.getSoulCrystalOptions().size()); // Size of regular soul crystal options.
		for (EnsoulOption option : item.getSoulCrystalOptions())
		{
			packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
		}
		
		packet.writeC(item.getSoulCrystalSpecialOptions().size()); // Size of special soul crystal options.
		for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
		{
			packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
		}
	}
}
