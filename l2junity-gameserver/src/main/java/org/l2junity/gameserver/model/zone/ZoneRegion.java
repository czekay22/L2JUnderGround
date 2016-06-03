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
package org.l2junity.gameserver.model.zone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.zone.type.PeaceZone;

/**
 * @author Nos
 */
public class ZoneRegion
{
	private final int _regionX;
	private final int _regionY;
	private final Map<Integer, ZoneType> _zones = new ConcurrentHashMap<>();
	
	public ZoneRegion(int regionX, int regionY)
	{
		_regionX = regionX;
		_regionY = regionY;
	}
	
	public Map<Integer, ZoneType> getZones()
	{
		return _zones;
	}
	
	public int getRegionX()
	{
		return _regionX;
	}
	
	public int getRegionY()
	{
		return _regionY;
	}
	
	public void revalidateZones(Creature character)
	{
		// do NOT update the world region while the character is still in the process of teleporting
		// Once the teleport is COMPLETED, revalidation occurs safely, at that time.
		
		if (character.isTeleporting())
		{
			return;
		}
		
		for (ZoneType z : getZones().values())
		{
			z.revalidateInZone(character);
		}
	}
	
	public void removeFromZones(Creature character)
	{
		for (ZoneType z : getZones().values())
		{
			z.removeCharacter(character);
		}
	}
	
	public boolean checkEffectRangeInsidePeaceZone(Skill skill, final int x, final int y, final int z)
	{
		final int range = skill.getEffectRange();
		final int up = y + range;
		final int down = y - range;
		final int left = x + range;
		final int right = x - range;
		
		for (ZoneType e : getZones().values())
		{
			if (e instanceof PeaceZone)
			{
				if (e.isInsideZone(x, up, z))
				{
					return false;
				}
				
				if (e.isInsideZone(x, down, z))
				{
					return false;
				}
				
				if (e.isInsideZone(left, y, z))
				{
					return false;
				}
				
				if (e.isInsideZone(right, y, z))
				{
					return false;
				}
				
				if (e.isInsideZone(x, y, z))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public void onDeath(Creature character)
	{
		for (ZoneType z : getZones().values())
		{
			if (z.isInsideZone(character))
			{
				z.onDieInside(character);
			}
		}
	}
	
	public void onRevive(Creature character)
	{
		for (ZoneType z : getZones().values())
		{
			if (z.isInsideZone(character))
			{
				z.onReviveInside(character);
			}
		}
	}
}
