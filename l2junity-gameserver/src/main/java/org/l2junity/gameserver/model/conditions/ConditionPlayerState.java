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
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.base.PlayerState;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * The Class ConditionPlayerState.
 * @author mkizub
 */
public class ConditionPlayerState extends Condition
{
	private final PlayerState _check;
	private final boolean _required;
	
	/**
	 * Instantiates a new condition player state.
	 * @param check the player state to be verified.
	 * @param required the required value.
	 */
	public ConditionPlayerState(PlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		final PlayerInstance player = effector.getActingPlayer();
		switch (_check)
		{
			case RESTING:
				if (player != null)
				{
					return (player.isSitting() == _required);
				}
				return !_required;
			case MOVING:
				return effector.isMoving() == _required;
			case RUNNING:
				return effector.isRunning() == _required;
			case STANDING:
				if (player != null)
				{
					return (_required != (player.isSitting() || player.isMoving()));
				}
				return (_required != effector.isMoving());
			case FLYING:
				return (effector.isFlying() == _required);
			case BEHIND:
				return (effector.isBehindTarget() == _required);
			case FRONT:
				return (effector.isInFrontOfTarget() == _required);
			case CHAOTIC:
				if (player != null)
				{
					return ((player.getReputation() < 0) == _required);
				}
				return !_required;
			case OLYMPIAD:
				if (player != null)
				{
					return (player.isInOlympiadMode() == _required);
				}
				return !_required;
		}
		return !_required;
	}
}
