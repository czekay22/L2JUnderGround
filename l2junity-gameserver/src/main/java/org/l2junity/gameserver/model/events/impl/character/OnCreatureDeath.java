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
package org.l2junity.gameserver.model.events.impl.character;

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.IBaseEvent;

/**
 * An instantly executed event when L2Character is killed by L2Character.
 * @author UnAfraid
 */
public class OnCreatureDeath implements IBaseEvent
{
	private final Creature _attacker;
	private final Creature _target;
	
	public OnCreatureDeath(Creature attacker, Creature target)
	{
		_attacker = attacker;
		_target = target;
	}
	
	public final Creature getAttacker()
	{
		return _attacker;
	}
	
	public final Creature getTarget()
	{
		return _target;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_DEATH;
	}
}