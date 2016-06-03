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
package handlers.effecthandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.stats.MoveType;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * StatByMoveType effect implementation.
 * @author UnAfraid
 */
public class StatByMoveType extends AbstractEffect
{
	private final Stats _stat;
	private final MoveType _type;
	private final double _value;
	
	public StatByMoveType(StatsSet params)
	{
		_stat = params.getEnum("stat", Stats.class);
		_type = params.getEnum("type", MoveType.class);
		_value = params.getDouble("value");
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().getStat().mergeMoveTypeValue(_stat, _type, _value);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getStat().mergeMoveTypeValue(_stat, _type, -_value);
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		return info.getSkill().isPassive() || info.getSkill().isToggle();
	}
}
