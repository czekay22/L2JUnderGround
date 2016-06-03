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
package org.l2junity.gameserver.model.conditions;

import java.util.Set;

import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * Condition Category Type implementation.
 * @author Adry_85
 */
public class ConditionCategoryType extends Condition
{
	private final Set<CategoryType> _categoryTypes;
	
	public ConditionCategoryType(Set<CategoryType> categoryTypes)
	{
		_categoryTypes = categoryTypes;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		for (CategoryType type : _categoryTypes)
		{
			if (effector.isInCategory(type))
			{
				return true;
			}
		}
		return false;
	}
}
