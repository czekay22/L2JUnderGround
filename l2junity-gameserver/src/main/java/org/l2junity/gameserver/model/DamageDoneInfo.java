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
package org.l2junity.gameserver.model;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author xban1x
 */
public final class DamageDoneInfo
{
	private final PlayerInstance _attacker;
	private int _damage = 0;
	
	public DamageDoneInfo(PlayerInstance attacker)
	{
		_attacker = attacker;
	}
	
	public PlayerInstance getAttacker()
	{
		return _attacker;
	}
	
	public void addDamage(int damage)
	{
		_damage += damage;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof DamageDoneInfo)
		{
			return (((DamageDoneInfo) obj).getAttacker() == _attacker);
		}
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _attacker.getObjectId();
	}
}
