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
package org.l2junity.gameserver.pathfinding.geonodes;

import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.pathfinding.AbstractNodeLoc;

/**
 * @author -Nemesiss-
 */
public class GeoNodeLoc extends AbstractNodeLoc
{
	private final short _x;
	private final short _y;
	private final short _z;
	
	public GeoNodeLoc(short x, short y, short z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public int getX()
	{
		return World.MAP_MIN_X + (_x * 128) + 48;
	}
	
	@Override
	public int getY()
	{
		return World.MAP_MIN_Y + (_y * 128) + 48;
	}
	
	@Override
	public int getZ()
	{
		return _z;
	}

	@Override
	public int getNodeX()
	{
		return _x;
	}
	
	@Override
	public int getNodeY()
	{
		return _y;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + _x;
		result = (prime * result) + _y;
		result = (prime * result) + _z;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof GeoNodeLoc))
		{
			return false;
		}
		final GeoNodeLoc other = (GeoNodeLoc) obj;
		if (_x != other._x)
		{
			return false;
		}
		if (_y != other._y)
		{
			return false;
		}
		if (_z != other._z)
		{
			return false;
		}
		return true;
	}
}
