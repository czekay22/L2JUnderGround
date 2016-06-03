/*
 * Copyright (C) 2004-2016 L2J Unity
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
package handlers.effecthandlers;

import org.l2junity.gameserver.enums.StatModifierType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.conditions.Condition;
import org.l2junity.gameserver.model.conditions.ConditionUsingItemType;
import org.l2junity.gameserver.model.conditions.ConditionUsingSlotType;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.type.WeaponType;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author Sdw
 */
public class TwoHandedBluntBonus extends AbstractEffect
{
	private final static Condition _weaponTypeCondition = new ConditionUsingItemType(WeaponType.BLUNT.mask());
	private final static Condition _slotCondition = new ConditionUsingSlotType(L2Item.SLOT_LR_HAND);
	
	protected final double _pAtkAmount;
	protected final StatModifierType _pAtkmode;
	
	protected final double _accuracyAmount;
	protected final StatModifierType _accuracyMode;
	
	public TwoHandedBluntBonus(StatsSet params)
	{
		_pAtkAmount = params.getDouble("pAtkAmount", 0);
		_pAtkmode = params.getEnum("pAtkmode", StatModifierType.class, StatModifierType.DIFF);
		
		_accuracyAmount = params.getDouble("accuracyAmount", 0);
		_accuracyMode = params.getEnum("accuracyMode", StatModifierType.class, StatModifierType.DIFF);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		if (((_weaponTypeCondition == null) || _weaponTypeCondition.test(effected, effected, skill)) && ((_slotCondition == null) || _slotCondition.test(effected, effected, skill)))
		{
			switch (_pAtkmode)
			{
				case DIFF:
				{
					effected.getStat().mergeAdd(Stats.PHYSICAL_ATTACK, _pAtkAmount);
					break;
				}
				case PER:
				{
					effected.getStat().mergeMul(Stats.PHYSICAL_ATTACK, (_pAtkAmount / 100) + 1);
					break;
				}
			}
			
			switch (_accuracyMode)
			{
				case DIFF:
				{
					effected.getStat().mergeAdd(Stats.ACCURACY_COMBAT, _accuracyAmount);
					break;
				}
				case PER:
				{
					effected.getStat().mergeMul(Stats.ACCURACY_COMBAT, (_accuracyAmount / 100) + 1);
					break;
				}
			}
		}
	}
}
