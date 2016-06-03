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
package org.l2junity.gameserver.model.cubic.conditions;

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.cubic.CubicInstance;

/**
 * @author UnAfraid
 */
public class GeneralCondition implements ICubicCondition
{
	private final GeneralConditionType _type;
	private final int _hpPer;
	private final int _hp;
	
	public GeneralCondition(GeneralConditionType type, int hpPer, int hp)
	{
		_type = type;
		_hpPer = hpPer;
		_hp = hp;
	}
	
	@Override
	public boolean test(CubicInstance cubic, Creature owner, Creature target)
	{
		final double hpPer = target.getCurrentHpPercent();
		switch (_type)
		{
			case GREATER:
			{
				if (hpPer < _hpPer)
				{
					return false;
				}
				if (target.getCurrentHp() < _hp)
				{
					return false;
				}
				break;
			}
			case LESSER:
			{
				if (hpPer > _hpPer)
				{
					return false;
				}
				if (target.getCurrentHp() > _hp)
				{
					return false;
				}
				break;
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " chance: " + _hpPer + " range: " + _hp;
	}
	
	public static enum GeneralConditionType
	{
		GREATER,
		LESSER;
	}
}
