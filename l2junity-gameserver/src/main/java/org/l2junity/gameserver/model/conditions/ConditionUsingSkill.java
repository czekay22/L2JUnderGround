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

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * The Class ConditionUsingSkill.
 * @author mkizub
 */
public final class ConditionUsingSkill extends Condition
{
	private final int _skillId;
	
	/**
	 * Instantiates a new condition using skill.
	 * @param skillId the skill id
	 */
	public ConditionUsingSkill(int skillId)
	{
		_skillId = skillId;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		if (skill == null)
		{
			return false;
		}
		return skill.getId() == _skillId;
	}
}
