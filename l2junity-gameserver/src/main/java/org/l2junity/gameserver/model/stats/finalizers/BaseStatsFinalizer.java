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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.l2junity.gameserver.data.xml.impl.ArmorSetsData;
import org.l2junity.gameserver.model.ArmorSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.stats.BaseStats;
import org.l2junity.gameserver.model.stats.IStatsFunction;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class BaseStatsFinalizer implements IStatsFunction
{
	@Override
	public double calc(Creature creature, Optional<Double> base, Stats stat)
	{
		throwIfPresent(base);
		
		// Apply template value
		double baseValue = creature.getTemplate().getBaseValue(stat, 0);
		
		final PlayerInstance player = creature.getActingPlayer();
		if (player != null)
		{
			final Set<ArmorSet> appliedSets = new HashSet<>(2);
			
			// Armor sets calculation
			for (ItemInstance item : player.getInventory().getPaperdollItems())
			{
				for (ArmorSet set : ArmorSetsData.getInstance().getSets(item.getId()))
				{
					if ((set.getPiecesCount(player, ItemInstance::getId) >= set.getMinimumPieces()) && appliedSets.add(set))
					{
						baseValue += set.getStatsBonus(BaseStats.valueOf(stat));
					}
				}
			}
			
			// Henna calculation
			baseValue += player.getHennaValue(BaseStats.valueOf(stat));
		}
		return validateValue(creature, Stats.defaultValue(creature, stat, baseValue), 1, BaseStats.MAX_STAT_VALUE - 1);
	}
	
}
