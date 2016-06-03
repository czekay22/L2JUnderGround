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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.data.xml.impl.AdminData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public abstract class AbstractServerPlugin
{
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final List<ServerPluginInstallFile> _filesToInstall = new ArrayList<>();
	private final List<ServerPluginInstallSQLFile> _databaseTablesToInstall = new ArrayList<>();
	private final AtomicBoolean _isStarted = new AtomicBoolean();
	
	/**
	 * @return the name of the plugin, used for versioning also!
	 */
	public abstract String getName();
	
	/**
	 * @return the author of the plugin, just for informational purpose!
	 */
	public abstract String getAuthor();
	
	/**
	 * @return the version of the plugin, if there are two plugins with same name the one with higher version will be used!
	 */
	public abstract int getVersion();
	
	/**
	 * @return the priority of execution, the higher the earlier!
	 */
	public int getPriority()
	{
		return 0;
	}
	
	/**
	 * Registers file to be copied to server
	 * @param file
	 */
	protected void addFile(ServerPluginInstallFile file)
	{
		_filesToInstall.add(file);
	}
	
	/**
	 * @return an Unmodifiable list of all files that are prepared to be installed on the server
	 */
	public List<ServerPluginInstallFile> getFiles()
	{
		return Collections.unmodifiableList(_filesToInstall);
	}
	
	/**
	 * Registers database table to be installed on the server
	 * @param file
	 */
	protected void addTable(ServerPluginInstallSQLFile file)
	{
		_databaseTablesToInstall.add(file);
	}
	
	/**
	 * @return an Unmodifiable list of all database tables that are prepared to be installed on the server
	 */
	public List<ServerPluginInstallSQLFile> getTables()
	{
		return Collections.unmodifiableList(_databaseTablesToInstall);
	}
	
	/**
	 * Installs the plugin on the server
	 * @return {@code true} if installation is successful, {@code false} otherwise
	 */
	public boolean install()
	{
		broadcastMessage("Plugin: " + getName() + " is installing files..");
		try
		{
			installFiles();
			installTables();
		}
		catch (Exception e)
		{
			LOGGER.warn("Plugin: " + getName() + " couldn't install plugin: files {}", getName(), _filesToInstall, e);
			return false;
		}
		broadcastMessage("Plugin: " + getName() + " installation complete!");
		return true;
	}
	
	/**
	 * @return {@code true} if uninstallation is successful, {@code false} otherwise
	 */
	public boolean uninstall()
	{
		broadcastMessage("Plugin: " + getName() + " is uninstalling files..");
		try
		{
			uninstallFiles();
		}
		catch (SecurityException e)
		{
			LOGGER.warn("Plugin: " + getName() + " couldn't uninstall plugin: files {}", getName(), _filesToInstall, e);
			return false;
		}
		broadcastMessage("Plugin: " + getName() + " uninstallation complete!");
		return true;
	}
	
	/**
	 * @param fromVersion
	 * @return
	 */
	public boolean update(int fromVersion)
	{
		return true;
	}
	
	/**
	 * Invoked in the initialization of game server
	 */
	public void onInit()
	{
	}
	
	/**
	 * Invoked during the scripts loading
	 */
	public void onLoad()
	{
	}
	
	/**
	 * Invoked only from admin menu or telnet in order to reload configuration
	 */
	public void onReload()
	{
	}
	
	/**
	 * Invoked at the end of game server boot
	 */
	public abstract void onStart();
	
	/**
	 * Invoked at game server shutdown/restart
	 */
	public abstract void onShutdown();
	
	/**
	 * Installs all prepared files
	 * @throws IOException
	 */
	protected void installFiles() throws IOException
	{
		for (ServerPluginInstallFile file : _filesToInstall)
		{
			final File destFile;
			if (file.getDestination().startsWith("config/"))
			{
				destFile = new File(file.getDestination());
			}
			else
			{
				destFile = new File(Config.DATAPACK_ROOT, file.getDestination());
			}
			
			// Create parent directories
			if (!destFile.getParentFile().exists())
			{
				destFile.getParentFile().mkdirs();
			}
			
			// Copy file
			try (InputStream source = getClass().getResourceAsStream(file.getSource()))
			{
				Files.copy(source, destFile.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
				onFileInstalled(file);
			}
		}
	}
	
	/**
	 * Installs all prepared database tables
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void installTables() throws SQLException, IOException
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement())
		{
			// Prevent half-way execution
			con.setAutoCommit(false);
			
			for (ServerPluginInstallSQLFile file : _databaseTablesToInstall)
			{
				String currentDatabase = "";
				try (ResultSet rs = st.executeQuery("SELECT DATABASE()"))
				{
					if (rs.next())
					{
						currentDatabase = rs.getString(1);
					}
				}
				
				if ((file.getDatabase() != null) && !currentDatabase.equalsIgnoreCase(file.getDatabase()))
				{
					// Switch database
					st.execute("USE " + file.getDatabase());
				}
				
				// Check for table existence
				try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?"))
				{
					ps.setString(1, file.getTable());
					try (ResultSet rs = ps.executeQuery())
					{
						if (!rs.next())
						{
							try (InputStream inputStream = getClass().getResourceAsStream(file.getSource());
								InputStreamReader reader = new InputStreamReader(inputStream);
								Scanner scn = new Scanner(reader))
							{
								StringBuilder sb = new StringBuilder();
								while (scn.hasNextLine())
								{
									String line = scn.nextLine();
									if (line.startsWith("--"))
									{
										continue;
									}
									else if (line.contains("--"))
									{
										line = line.split("--")[0];
									}
									
									line = line.trim();
									if (!line.isEmpty())
									{
										sb.append(line + System.lineSeparator());
									}
									
									if (line.endsWith(";"))
									{
										st.execute(sb.toString());
										sb = new StringBuilder();
									}
								}
							}
						}
					}
				}
				
				if ((file.getDatabase() != null) && !currentDatabase.equalsIgnoreCase(file.getDatabase()))
				{
					// Switch database back to its original state
					st.execute("USE " + currentDatabase);
				}
				// Notify about new table installed successfully
				onNewDatabaseTableInstalled(file);
			}
			
			if (!con.getAutoCommit())
			{
				con.commit();
				con.setAutoCommit(true);
			}
		}
	}
	
	/**
	 * Uninstalls all prepared files
	 * @throws SecurityException
	 */
	protected void uninstallFiles() throws SecurityException
	{
		for (ServerPluginInstallFile file : _filesToInstall)
		{
			final File destFile;
			if (file.getDestination().startsWith("config/"))
			{
				destFile = new File(file.getDestination());
			}
			else
			{
				destFile = new File(Config.DATAPACK_ROOT, file.getDestination());
			}
			
			destFile.delete();
			onFileUninstalled(file);
		}
	}
	
	/**
	 * Invoked upon successfully installed file
	 * @param file
	 */
	protected void onFileInstalled(ServerPluginInstallFile file)
	{
		broadcastMessage("New file: " + file.getDestination());
	}
	
	/**
	 * Invoked upon successfully uninstalled file
	 * @param file
	 */
	protected void onFileUninstalled(ServerPluginInstallFile file)
	{
		broadcastMessage("File removed: " + file.getDestination());
	}
	
	/**
	 * Invoked upon successfully installed database table
	 * @param file
	 */
	protected void onNewDatabaseTableInstalled(ServerPluginInstallSQLFile file)
	{
		broadcastMessage("New database table: " + file.getTable() + " on database: " + file.getDatabase());
	}
	
	/**
	 * Method that will send message to all online gms and console
	 * @param message
	 */
	protected void broadcastMessage(String message)
	{
		AdminData.getInstance().broadcastMessageToGMs(message);
		LOGGER.info(message);
	}
	
	/**
	 * @return {@code true} if plugin is currently started, {@code false} otherwise
	 */
	public final boolean isStarted()
	{
		return _isStarted.get();
	}
	
	/**
	 * @param value
	 * @return @return {@code true} if plugin started value is successfully changed, {@code false} otherwise
	 */
	public final boolean setStarted(boolean value)
	{
		return _isStarted.compareAndSet(!value, value);
	}
}
