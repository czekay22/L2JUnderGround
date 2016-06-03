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

import org.l2junity.commons.util.MathUtil;
import org.l2junity.gameserver.enums.Position;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author Sdw
 */
public class CriticalRatePositionBonus extends AbstractEffect
{
	protected final double _amount;
	protected final Position _position;
	
	public CriticalRatePositionBonus(StatsSet params)
	{
		_amount = params.getDouble("amount", 0);
		_position = params.getEnum("position", Position.class, Position.FRONT);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().getStat().mergePositionTypeValue(Stats.CRITICAL_RATE, _position, (_amount / 100) + 1, MathUtil::mul);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getStat().mergePositionTypeValue(Stats.CRITICAL_RATE, _position, (-_amount / 100) - 1, MathUtil::div);
	}
}
