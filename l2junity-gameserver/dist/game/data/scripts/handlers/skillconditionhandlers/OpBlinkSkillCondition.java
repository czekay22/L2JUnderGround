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

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.enums.Position;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.util.Util;

/**
 * @author Sdw
 */
public class OpBlinkSkillCondition implements ISkillCondition
{
	private final int _angle;
	private final int _range;
	
	public OpBlinkSkillCondition(StatsSet params)
	{
		switch (params.getEnum("direction", Position.class))
		{
			case BACK:
			{
				_angle = 0;
				break;
			}
			case FRONT:
			{
				_angle = 180;
				break;
			}
			default:
			{
				_angle = -1;
				break;
			}
		}
		
		_range = params.getInt("range");
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		final double angle = Util.convertHeadingToDegree(caster.getHeading());
		final double radian = Math.toRadians(angle);
		final double course = Math.toRadians(_angle);
		final int x1 = (int) (Math.cos(Math.PI + radian + course) * _range);
		final int y1 = (int) (Math.sin(Math.PI + radian + course) * _range);
		
		int x = caster.getX() + x1;
		int y = caster.getY() + y1;
		int z = caster.getZ();
		
		return GeoData.getInstance().canMove(caster.getX(), caster.getY(), caster.getZ(), x, y, z, caster.getInstanceWorld());
	}
}
