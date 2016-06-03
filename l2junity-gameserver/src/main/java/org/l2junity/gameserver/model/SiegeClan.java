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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.enums.SiegeClanType;
import org.l2junity.gameserver.model.actor.Npc;

public class SiegeClan
{
	private int _clanId = 0;
	private final Set<Npc> _flags = ConcurrentHashMap.newKeySet();
	private SiegeClanType _type;
	
	public SiegeClan(int clanId, SiegeClanType type)
	{
		_clanId = clanId;
		_type = type;
	}
	
	public int getNumFlags()
	{
		return _flags.size();
	}
	
	public void addFlag(Npc flag)
	{
		_flags.add(flag);
	}
	
	public boolean removeFlag(Npc flag)
	{
		if (flag == null)
		{
			return false;
		}
		
		flag.deleteMe();
		
		return getFlag().remove(flag);
	}
	
	public void removeFlags()
	{
		for (Npc flag : getFlag())
		{
			removeFlag(flag);
		}
	}
	
	public final int getClanId()
	{
		return _clanId;
	}
	
	public final Set<Npc> getFlag()
	{
		return _flags;
	}
	
	public SiegeClanType getType()
	{
		return _type;
	}
	
	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}
}
