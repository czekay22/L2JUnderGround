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
package org.l2junity.gameserver.model.events.impl.character;

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.IBaseEvent;

/**
 * @author UnAfraid
 */
public class OnCreatureHpChange implements IBaseEvent
{
	private final Creature _creature;
	private final double _newHp;
	private final double _oldHp;
	
	public OnCreatureHpChange(Creature creature, double oldHp, double newHp)
	{
		_creature = creature;
		_oldHp = oldHp;
		_newHp = newHp;
	}
	
	public Creature getCreature()
	{
		return _creature;
	}
	
	public double getOldHp()
	{
		return _oldHp;
	}
	
	public double getNewHp()
	{
		return _newHp;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_HP_CHANGE;
	}
}
