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
package org.l2junity.gameserver.model.holders;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * @author St3eT
 */
public class ClanHallTeleportHolder extends Location
{
	private final NpcStringId _npcStringId;
	private final int _minFunctionLevel;
	private final int _cost;
	
	public ClanHallTeleportHolder(int npcStringId, int x, int y, int z, int minFunctionLevel, int cost)
	{
		super(x, y, z);
		_npcStringId = NpcStringId.getNpcStringId(npcStringId);
		_minFunctionLevel = minFunctionLevel;
		_cost = cost;
	}
	
	public final NpcStringId getNpcStringId()
	{
		return _npcStringId;
	}
	
	public final int getMinFunctionLevel()
	{
		return _minFunctionLevel;
	}
	
	public final int getCost()
	{
		return _cost;
	}
}
