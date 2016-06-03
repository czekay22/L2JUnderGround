/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.SkillCaster;

/**
 * Dam Over Time effect implementation.
 */
public final class CallSkillOnActionTime extends AbstractEffect
{
	private final SkillHolder _skill;
	
	public CallSkillOnActionTime(StatsSet params)
	{
		_skill = new SkillHolder(params.getInt("skillId"), params.getInt("skillLevel", 1));
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		return castSkill(info);
	}
	
	private boolean castSkill(BuffInfo info)
	{
		if (info.getEffector().isDead())
		{
			return false;
		}
		
		final Skill skill = _skill.getSkill();
		if (skill != null)
		{
			if (skill.isSynergySkill())
			{
				skill.applyEffects(info.getEffector(), info.getEffector());
			}
			
			World.getInstance().forEachVisibleObjectInRange(info.getEffector(), Creature.class, _skill.getSkill().getAffectRange(), c ->
			{
				final WorldObject target = skill.getTarget(info.getEffector(), c, false, false, false);
				
				if ((target != null) && target.isCreature())
				{
					SkillCaster.triggerCast(info.getEffector(), (Creature) target, skill);
				}
			});
		}
		else
		{
			_log.warn("Skill not found effect called from {}", info.getSkill());
		}
		return info.getSkill().isToggle();
	}
}
