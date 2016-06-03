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
package handlers.skillconditionhandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * @author UnAfraid
 */
public class OpCheckAbnormalSkillCondition implements ISkillCondition
{
	private final AbnormalType _type;
	private final int _level;
	private final boolean _hasAbnormal;
	
	public OpCheckAbnormalSkillCondition(StatsSet params)
	{
		_type = params.getEnum("type", AbnormalType.class);
		_level = params.getInt("level");
		_hasAbnormal = params.getBoolean("hasAbnormal");
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (target.isCreature())
		{
			final BuffInfo info = ((Creature) target).getEffectList().getBuffInfoByAbnormalType(_type);
			if (_hasAbnormal)
			{
				return (info != null) && (info.getSkill().getAbnormalLvl() >= _level);
			}
			return (info == null) || (info.getSkill().getAbnormalLvl() < _level);
		}
		return false;
	}
}
