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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public final class ServerPluginProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerPluginProvider.class);
	
	private final List<AbstractServerPlugin> _allPlugins = new ArrayList<>();
	private final List<AbstractServerPlugin> _activePlugins = new ArrayList<>();
	
	protected ServerPluginProvider()
	{
		init();
	}
	
	private void init()
	{
		final ServiceLoader<AbstractServerPlugin> provider = ServiceLoader.load(AbstractServerPlugin.class);
		final Map<String, Integer> versions = new HashMap<>();
		for (AbstractServerPlugin plugin : provider)
		{
			// Skip older plugins
			final ServerPlugin serverPlugin = PluginsTable.getInstance().getPlugin(plugin.getName());
			if ((serverPlugin != null) && (serverPlugin.getVersion() > plugin.getVersion()))
			{
				continue;
			}
			
			if (versions.containsKey(plugin.getName()))
			{
				final double version = versions.get(plugin.getName());
				if (version < plugin.getVersion())
				{
					_allPlugins.add(plugin);
					versions.put(plugin.getName(), plugin.getVersion());
					LOGGER.info("Replacing plugin + " + plugin.getName() + " new version: " + plugin.getVersion() + " old version: " + version);
				}
			}
			else
			{
				_allPlugins.add(plugin);
				versions.put(plugin.getName(), plugin.getVersion());
			}
		}
		
		//@formatter:off
		_allPlugins.stream()
			.sorted(Comparator.comparingInt(AbstractServerPlugin::getPriority).reversed())
			.filter(PluginsTable.getInstance()::hasPlugin)
			.filter(Objects::nonNull)
			.forEach(_activePlugins::add);
		//@formatter:on
		LOGGER.info("Loaded " + _allPlugins.size() + " plugins");
	}
	
	public List<AbstractServerPlugin> getPlugins()
	{
		return _allPlugins;
	}
	
	public void onInit()
	{
		try
		{
			_activePlugins.forEach(AbstractServerPlugin::onInit);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to notify onInit", e);
		}
	}
	
	public void onLoad()
	{
		try
		{
			_activePlugins.forEach(AbstractServerPlugin::onLoad);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to notify onLoad", e);
		}
	}
	
	public void onReload()
	{
		try
		{
			_activePlugins.forEach(AbstractServerPlugin::onReload);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to notify onReload", e);
		}
	}
	
	public void onStart()
	{
		try
		{
			_activePlugins.forEach(AbstractServerPlugin::onStart);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to notify onStart", e);
		}
	}
	
	public void onShutdown()
	{
		try
		{
			_activePlugins.forEach(AbstractServerPlugin::onShutdown);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to notify onShutdown", e);
		}
	}
	
	/**
	 * @param pluginName
	 * @return {@code true} if plugin is installed and started, {@code false} otherwise
	 */
	public boolean isPluginStarted(String pluginName)
	{
		final ServerPlugin installedPlugin = PluginsTable.getInstance().getPlugin(pluginName);
		if (installedPlugin != null)
		{
			final AbstractServerPlugin plugin = _activePlugins.stream().filter(p -> p.getName().equalsIgnoreCase(installedPlugin.getName()) && (p.getVersion() == installedPlugin.getVersion())).findFirst().orElse(null);
			return (plugin != null) && plugin.isStarted();
		}
		return false;
	}
	
	/**
	 * @param pluginName
	 * @return {@code true} if plugin is installed, {@code false} otherwise
	 */
	public boolean isPluginInstalled(String pluginName)
	{
		return PluginsTable.getInstance().getPlugin(pluginName) != null;
	}
	
	/**
	 * Gets the single instance of ServerPluginProvider.
	 * @return single instance of ServerPluginProvider
	 */
	public static ServerPluginProvider getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerPluginProvider INSTANCE = new ServerPluginProvider();
	}
}
