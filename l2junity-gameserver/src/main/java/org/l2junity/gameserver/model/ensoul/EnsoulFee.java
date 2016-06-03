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
package org.l2junity.gameserver.model.ensoul;

import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.items.type.CrystalType;

/**
 * @author UnAfraid
 */
public class EnsoulFee
{
	private final CrystalType _type;
	
	private final ItemHolder[] _ensoulFee = new ItemHolder[3];
	private final ItemHolder[] _resoulFees = new ItemHolder[3];
	
	public EnsoulFee(CrystalType type)
	{
		_type = type;
	}
	
	public CrystalType getCrystalType()
	{
		return _type;
	}
	
	public void setEnsoul(int index, ItemHolder item)
	{
		_ensoulFee[index] = item;
	}
	
	public void setResoul(int index, ItemHolder item)
	{
		_resoulFees[index] = item;
	}
	
	public ItemHolder getEnsoul(int index)
	{
		return _ensoulFee[index];
	}
	
	public ItemHolder getResoul(int index)
	{
		return _resoulFees[index];
	}
}
