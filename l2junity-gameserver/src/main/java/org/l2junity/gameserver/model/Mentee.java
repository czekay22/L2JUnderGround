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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class Mentee
{
	private static final Logger _log = LoggerFactory.getLogger(Mentee.class);
	
	private final int _objectId;
	private String _name;
	private int _classId;
	private int _currentLevel;
	
	public Mentee(int objectId)
	{
		_objectId = objectId;
		load();
	}
	
	public void load()
	{
		PlayerInstance player = getPlayerInstance();
		if (player == null) // Only if player is offline
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT char_name, level, base_class FROM characters WHERE charId = ?"))
			{
				statement.setInt(1, getObjectId());
				try (ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						_name = rset.getString("char_name");
						_classId = rset.getInt("base_class");
						_currentLevel = rset.getInt("level");
					}
				}
			}
			catch (Exception e)
			{
				_log.warn(e.getMessage(), e);
			}
		}
		else
		{
			_name = player.getName();
			_classId = player.getBaseClass();
			_currentLevel = player.getLevel();
		}
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getClassId()
	{
		if (isOnline())
		{
			if (getPlayerInstance().getClassId().getId() != _classId)
			{
				_classId = getPlayerInstance().getClassId().getId();
			}
		}
		return _classId;
	}
	
	public int getLevel()
	{
		if (isOnline())
		{
			if (getPlayerInstance().getLevel() != _currentLevel)
			{
				_currentLevel = getPlayerInstance().getLevel();
			}
		}
		return _currentLevel;
	}
	
	public PlayerInstance getPlayerInstance()
	{
		return World.getInstance().getPlayer(_objectId);
	}
	
	public boolean isOnline()
	{
		return (getPlayerInstance() != null) && (getPlayerInstance().isOnlineInt() > 0);
	}
	
	public int isOnlineInt()
	{
		return isOnline() ? getPlayerInstance().isOnlineInt() : 0;
	}
	
	public void sendPacket(IClientOutgoingPacket packet)
	{
		if (isOnline())
		{
			getPlayerInstance().sendPacket(packet);
		}
	}
}
