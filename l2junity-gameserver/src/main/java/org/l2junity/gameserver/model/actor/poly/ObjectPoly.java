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
package org.l2junity.gameserver.model.actor.poly;

import org.l2junity.gameserver.model.WorldObject;

public class ObjectPoly
{
	private final WorldObject _activeObject;
	private int _polyId;
	private String _polyType;
	
	public ObjectPoly(WorldObject activeObject)
	{
		_activeObject = activeObject;
	}
	
	public void setPolyInfo(String polyType, String polyId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}
	
	public final WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	public final boolean isMorphed()
	{
		return getPolyType() != null;
	}
	
	public final int getPolyId()
	{
		return _polyId;
	}
	
	public final void setPolyId(int value)
	{
		_polyId = value;
	}
	
	public final String getPolyType()
	{
		return _polyType;
	}
	
	public final void setPolyType(String value)
	{
		_polyType = value;
	}
}
