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
package org.l2junity.gameserver.model.stats.finalizers;

import java.util.Optional;

import org.l2junity.Config;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.stats.BaseStats;
import org.l2junity.gameserver.model.stats.IStatsFunction;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class PAttackFinalizer implements IStatsFunction
{
	@Override
	public double calc(Creature creature, Optional<Double> base, Stats stat)
	{
		throwIfPresent(base);
		
		double baseValue = calcWeaponBaseValue(creature, stat);
		baseValue += calcEnchantedItemBonus(creature, stat);
		
		if (creature.isPlayer())
		{
			// Enchanted chest bonus
			baseValue += calcEnchantBodyPart(creature, L2Item.SLOT_CHEST, L2Item.SLOT_FULL_ARMOR);
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && creature.isChampion())
		{
			baseValue *= Config.L2JMOD_CHAMPION_ATK;
		}
		if (creature.isRaid())
		{
			baseValue *= Config.RAID_PATTACK_MULTIPLIER;
		}
		final double chaBonus = creature.isPlayer() ? BaseStats.CHA.calcBonus(creature) : 1.;
		final double strBonus = creature.getSTR() > 0 ? BaseStats.STR.calcBonus(creature) : 1.;
		baseValue *= strBonus * creature.getLevelMod() * chaBonus;
		return Stats.defaultValue(creature, stat, baseValue);
	}
	
	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		if (isBlessed)
		{
			return (3 * Math.max(enchantLevel - 3, 0)) + (3 * Math.max(enchantLevel - 6, 0));
		}
		
		return (2 * Math.max(enchantLevel - 3, 0)) + (2 * Math.max(enchantLevel - 6, 0));
	}
}
