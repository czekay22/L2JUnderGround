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
package org.l2junity.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.Armor;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.model.items.instance.ItemInstance;

public class PreparedListContainer extends ListContainer
{
	private int _npcObjectId = 0;
	
	public PreparedListContainer(ListContainer template, boolean inventoryOnly, PlayerInstance player, Npc npc)
	{
		super(template);
		
		setMaintainEnchantment(template.getMaintainEnchantment());
		setApplyTaxes(false);
		double taxRate = 0;
		if (npc != null)
		{
			_npcObjectId = npc.getObjectId();
			if (template.getApplyTaxes() && npc.isInTown() && (npc.getCastle().getOwnerId() > 0))
			{
				setApplyTaxes(true);
				taxRate = npc.getCastle().getTaxRate();
			}
		}
		
		if (inventoryOnly)
		{
			if (player == null)
			{
				return;
			}
			
			final Collection<ItemInstance> items;
			if (getMaintainEnchantment())
			{
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
			}
			else
			{
				items = player.getInventory().getUniqueItems(false, false, false);
			}
			
			_entries = new LinkedList<>();
			for (ItemInstance item : items)
			{
				// only do the match up on equippable items that are not currently equipped
				// so for each appropriate item, produce a set of entries for the multisell list.
				if (!item.isEquipped() && ((item.getItem() instanceof Armor) || (item.getItem() instanceof Weapon)))
				{
					// loop through the entries to see which ones we wish to include
					for (Entry ent : template.getEntries())
					{
						// check ingredients of this entry to see if it's an entry we'd like to include.
						for (Ingredient ing : ent.getIngredients())
						{
							if (item.getId() == ing.getItemId())
							{
								_entries.add(new PreparedEntry(ent, item, getApplyTaxes(), getMaintainEnchantment(), taxRate));
								break; // next entry
							}
						}
					}
				}
			}
		}
		else
		{
			_entries = new ArrayList<>(template.getEntries().size());
			for (Entry ent : template.getEntries())
			{
				_entries.add(new PreparedEntry(ent, null, getApplyTaxes(), false, taxRate));
			}
		}
	}
	
	public final boolean checkNpcObjectId(int npcObjectId)
	{
		return _npcObjectId == 0 || _npcObjectId == npcObjectId;
	}
}
