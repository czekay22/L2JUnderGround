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
package org.l2junity.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads name and access level for all players.
 * @version 2005/03/27
 */
public class CharNameTable
{
	private static Logger _log = LoggerFactory.getLogger(CharNameTable.class);
	
	private final Map<Integer, String> _chars = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> _accessLevels = new ConcurrentHashMap<>();
	
	protected CharNameTable()
	{
		if (Config.CACHE_CHAR_NAMES)
		{
			loadAll();
		}
	}
	
	public final void addName(PlayerInstance player)
	{
		if (player != null)
		{
			addName(player.getObjectId(), player.getName());
			_accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}
	
	private final void addName(int objectId, String name)
	{
		if (name != null)
		{
			if (!name.equals(_chars.get(objectId)))
			{
				_chars.put(objectId, name);
			}
		}
	}
	
	public final void removeName(int objId)
	{
		_chars.remove(objId);
		_accessLevels.remove(objId);
	}
	
	public final int getIdByName(String name)
	{
		if ((name == null) || name.isEmpty())
		{
			return -1;
		}
		
		for (Entry<Integer, String> entry : _chars.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(name))
			{
				return entry.getKey();
			}
		}
		
		if (Config.CACHE_CHAR_NAMES)
		{
			return -1;
		}
		
		int id = -1;
		int accessLevel = 0;
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					id = rs.getInt("charId");
					accessLevel = rs.getInt("accesslevel");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve class for id: {}", e.getMessage(), e);
		}
		
		if (id > 0)
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return id;
		}
		
		return -1; // not found
	}
	
	public final String getNameById(int id)
	{
		if (id <= 0)
		{
			return null;
		}
		
		String name = _chars.get(id);
		if (name != null)
		{
			return name;
		}
		
		if (Config.CACHE_CHAR_NAMES)
		{
			return null;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?"))
		{
			ps.setInt(1, id);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					name = rset.getString("char_name");
					_chars.put(id, name);
					_accessLevels.put(id, rset.getInt("accesslevel"));
					return name;
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve class for id: {}", e.getMessage(), e);
		}
		
		return null; // not found
	}
	
	public final int getAccessLevelById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _accessLevels.get(objectId);
		}
		
		return 0;
	}
	
	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = false;
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as count FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					result = rs.getInt("count") > 0;
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve char name for id: {}", e.getMessage(), e);
		}
		return result;
	}
	
	public int getAccountCharacterCount(String account)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(char_name) as count FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("count");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve account for id: {}", e.getMessage(), e);
		}
		return 0;
	}
	
	public int getLevelById(int objectId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT level FROM characters WHERE charId = ?"))
		{
			ps.setInt(1, objectId);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("level");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve level for id: {}", e.getMessage(), e);
		}
		return 0;
	}
	
	public int getClassIdById(int objectId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT classid FROM characters WHERE charId = ?"))
		{
			ps.setInt(1, objectId);
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("classid");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve class for id: {}" + e.getMessage(), e);
		}
		return 0;
	}
	
	public int getClanIdById(int objectId)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT clanId FROM characters WHERE charId = ?"))
		{
			ps.setInt(1, objectId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					return rset.getInt("clanId");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve clan for id: {}" + e.getMessage(), e);
		}
		return 0;
	}
	
	private void loadAll()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT charId, char_name, accesslevel FROM characters"))
		{
			while (rs.next())
			{
				final int id = rs.getInt("charId");
				_chars.put(id, rs.getString("char_name"));
				_accessLevels.put(id, rs.getInt("accesslevel"));
			}
		}
		catch (SQLException e)
		{
			_log.warn("Couldn't retrieve all char id/name/access: {}", e.getMessage(), e);
		}
		_log.info("Loaded " + _chars.size() + " char names.");
	}
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
}
