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
package org.l2junity.gameserver.model.zone.type;

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.tasks.player.FlyMoveStartTask;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;

/**
 * @author UnAfraid
 */
public class SayuneZone extends ZoneType
{
	private int _mapId = -1;
	
	public SayuneZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "mapId":
			{
				_mapId = Integer.parseInt(value);
				break;
			}
			default:
			{
				super.setParameter(name, value);
			}
		}
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character.isPlayer() && character.isInCategory(CategoryType.AWAKEN_GROUP) && !character.getActingPlayer().isMounted() && !character.isTransformed())
		{
			character.setInsideZone(ZoneId.SAYUNE, true);
			ThreadPoolManager.getInstance().executeGeneral(new FlyMoveStartTask(this, character.getActingPlayer()));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.SAYUNE, false);
		}
	}
	
	public int getMapId()
	{
		return _mapId;
	}
}
