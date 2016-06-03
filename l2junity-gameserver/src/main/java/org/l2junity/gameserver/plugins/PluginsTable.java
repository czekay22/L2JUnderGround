/*
 * Copyright (C) 2004-2015 L2J United
 * 
 * This file is part of L2J United.
 * 
 * L2J United is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J United is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class PluginsTable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsTable.class);
	private final Map<String, ServerPlugin> _plugins = new ConcurrentHashMap<>();
	
	protected PluginsTable()
	{
		load();
	}
	
	private void load()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM plugins"))
		{
			while (rs.next())
			{
				final String name = rs.getString("name");
				final int version = rs.getInt("version");
				final long installedOn = rs.getLong("installedOn");
				_plugins.put(name, new ServerPlugin(name, version, installedOn));
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Couldn't load plugins:", e);
		}
	}
	
	public boolean hasPlugin(AbstractServerPlugin plugin)
	{
		final ServerPlugin serverPlugin = _plugins.get(plugin.getName());
		return (serverPlugin != null) && (plugin.getVersion() <= serverPlugin.getVersion());
	}
	
	public ServerPlugin getPlugin(String name)
	{
		return _plugins.get(name);
	}
	
	public void addPlugin(String name, int version)
	{
		final long installedOn = System.currentTimeMillis();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO plugins (name, version, installedOn) VALUES (?, ?, ?)"))
		{
			ps.setString(1, name);
			ps.setInt(2, version);
			ps.setLong(3, installedOn);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warn("Couldn't add plugin: {}:{}", name, version, e);
		}
		finally
		{
			_plugins.put(name, new ServerPlugin(name, version, installedOn));
		}
	}
	
	public void removePlugin(String name, int version)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM plugins WHERE name = ? AND version = ?"))
		{
			ps.setString(1, name);
			ps.setInt(2, version);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warn("Couldn't remove plugin: {}:{}", name, version, e);
		}
		finally
		{
			_plugins.remove(name);
		}
	}
	
	/**
	 * Gets the single instance of PluginsTable.
	 * @return single instance of PluginsTable.
	 */
	public static PluginsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PluginsTable _instance = new PluginsTable();
	}
}
