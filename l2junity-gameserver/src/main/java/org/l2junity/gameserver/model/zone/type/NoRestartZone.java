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

import org.l2junity.gameserver.GameServer;
import org.l2junity.gameserver.model.TeleportWhereType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;

/**
 * A simple no restart zone
 * @author GKR
 */
public class NoRestartZone extends ZoneType
{
	private int _restartAllowedTime = 0;
	private int _restartTime = 0;
	private boolean _enabled = true;
	
	public NoRestartZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("default_enabled"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("restartAllowedTime"))
		{
			_restartAllowedTime = Integer.parseInt(value) * 1000;
		}
		else if (name.equalsIgnoreCase("restartTime"))
		{
			_restartTime = Integer.parseInt(value) * 1000;
		}
		else if (name.equalsIgnoreCase("instanceId"))
		{
			// Do nothing.
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_RESTART, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_RESTART, false);
		}
	}
	
	@Override
	public void onPlayerLoginInside(PlayerInstance player)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (((System.currentTimeMillis() - player.getLastAccess()) > getRestartTime()) && ((System.currentTimeMillis() - GameServer.getInstance().getStartedTime()) > getRestartAllowedTime()))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}
	}
	
	public int getRestartAllowedTime()
	{
		return _restartAllowedTime;
	}
	
	public void setRestartAllowedTime(int time)
	{
		_restartAllowedTime = time;
	}
	
	public int getRestartTime()
	{
		return _restartTime;
	}
	
	public void setRestartTime(int time)
	{
		_restartTime = time;
	}
}
