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

import org.l2junity.gameserver.enums.ReduceDropType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * @author Sdw
 */
public class ReduceDropPenalty extends AbstractEffect
{
	private final double _exp;
	private final double _deathPenalty;
	private final ReduceDropType _type;
	
	public ReduceDropPenalty(StatsSet params)
	{
		_exp = params.getDouble("exp", 0);
		_deathPenalty = params.getDouble("deathPenalty", 0);
		_type = params.getEnum("type", ReduceDropType.class, ReduceDropType.MOB);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		switch (_type)
		{
			case MOB:
			{
				effected.getStat().mergeMul(Stats.REDUCE_EXP_LOST_BY_MOB, (_exp / 100) + 1);
				effected.getStat().mergeMul(Stats.REDUCE_DEATH_PENALTY_BY_MOB, (_deathPenalty / 100) + 1);
				break;
			}
			case PK:
			{
				effected.getStat().mergeMul(Stats.REDUCE_EXP_LOST_BY_PVP, (_exp / 100) + 1);
				effected.getStat().mergeMul(Stats.REDUCE_DEATH_PENALTY_BY_PVP, (_deathPenalty / 100) + 1);
				break;
			}
			case RAID:
			{
				effected.getStat().mergeMul(Stats.REDUCE_EXP_LOST_BY_RAID, (_exp / 100) + 1);
				effected.getStat().mergeMul(Stats.REDUCE_DEATH_PENALTY_BY_RAID, (_deathPenalty / 100) + 1);
				break;
			}
		}
	}
}
