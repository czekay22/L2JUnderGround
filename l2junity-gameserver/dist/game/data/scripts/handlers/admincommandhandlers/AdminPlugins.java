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
package handlers.admincommandhandlers;

import org.l2junity.gameserver.cache.HtmCache;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.html.PageBuilder;
import org.l2junity.gameserver.model.html.PageResult;
import org.l2junity.gameserver.model.html.formatters.BypassParserFormatter;
import org.l2junity.gameserver.model.html.pagehandlers.NextPrevPageHandler;
import org.l2junity.gameserver.model.html.styles.ButtonsStyle;
import org.l2junity.gameserver.plugins.AbstractServerPlugin;
import org.l2junity.gameserver.plugins.PluginsTable;
import org.l2junity.gameserver.plugins.ServerPluginProvider;
import org.l2junity.gameserver.util.BypassParser;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class AdminPlugins implements IAdminCommandHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminPlugins.class);
	
	private static final String[] COMMANDS =
	{
		"admin_plugins"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final BypassParser parser = new BypassParser(command);
		final String action = parser.getString("action", "");
		final String plugin = parser.getString("plugin", null);
		switch (action)
		{
			case "install":
			{
				if (plugin != null)
				{
					installPlugin(activeChar, plugin);
				}
				break;
			}
			case "uninstall":
			{
				if (plugin != null)
				{
					uninstallPlugin(activeChar, plugin);
				}
				break;
			}
			case "start":
			{
				if (plugin != null)
				{
					startPlugin(activeChar, plugin);
				}
				break;
			}
			case "shutdown":
			{
				if (plugin != null)
				{
					shutdownPlugin(activeChar, plugin);
				}
				break;
			}
			case "reload":
			{
				if (plugin != null)
				{
					reloadPlugin(activeChar, plugin);
				}
				break;
			}
		}
		showPlugins(activeChar, parser);
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param parser
	 */
	private void showPlugins(PlayerInstance activeChar, BypassParser parser)
	{
		int page = parser.getInt("page", 0);
		String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/plugins.htm");
		if (html == null)
		{
			LOGGER.warn("Html data/html/admin/plugins.htm is missing!");
			return;
		}
		
		//@formatter:off
		final PageResult result = PageBuilder.newBuilder(ServerPluginProvider.getInstance().getPlugins(), 10, "bypass -h admin_plugins")
			.currentPage(page)
			.pageHandler(NextPrevPageHandler.INSTANCE)
			.formatter(BypassParserFormatter.INSTANCE)
			.style(ButtonsStyle.INSTANCE)
			.bodyHandler((pages, plugin, sb) ->
		{
			sb.append("<tr>");
			sb.append("<td width=\"5\"></td>");
			sb.append("<td width=\"200\">").append(plugin.getName()).append("</td>");
			sb.append("<td width=\"20\">").append(plugin.getVersion()).append("</td>");
			sb.append("<td width=\"100\">").append(plugin.getAuthor()).append("</td>");
			if (!PluginsTable.getInstance().hasPlugin(plugin))
			{
				sb.append("<td width=\"100\">").append("<a action=\"bypass admin_plugins action=install plugin=").append(plugin.getName()).append(" page=").append(page).append("\">Install</a>").append("</td>");
			}
			else
			{
				sb.append("<td width=\"100\">").append("<a action=\"bypass admin_plugins action=uninstall plugin=").append(plugin.getName()).append(" page=").append(page).append("\">Uninstall</a>").append("</td>");
			}
			if (!plugin.isStarted())
			{
				if (PluginsTable.getInstance().hasPlugin(plugin))
				{
					sb.append("<td width=\"100\">").append("<a action=\"bypass admin_plugins action=start plugin=").append(plugin.getName()).append(" page=").append(page).append("\">Start</a>").append("</td>");
				}
				else
				{
					sb.append("<td width=\"100\">Installation required</td>");
				}
			}
			else
			{
				sb.append("<td width=\"100\">").append("<a action=\"bypass admin_plugins action=shutdown plugin=").append(plugin.getName()).append(" page=").append(page).append("\">Shutdown</a> | ").append("<a action=\"bypass admin_plugins action=reload plugin=").append(plugin.getName()).append(" page=").append(page).append("\">Reload</a>").append("</td>");
			}
			sb.append("<td width=\"5\"></td>");
			sb.append("</tr>");
		}).build();
		//@formatter:on
		
		html = html.replaceAll("%pages%", result.getPages() > 0 ? ("<center><table width=\"100%\" cellspacing=0><tr>" + result.getPagerTemplate() + "</tr></table></center>") : "");
		html = html.replaceAll("%plugins%", result.getBodyTemplate().toString());
		Util.sendCBHtml(activeChar, html);
	}
	
	/**
	 * @param activeChar
	 * @param pluginName
	 */
	private void installPlugin(PlayerInstance activeChar, String pluginName)
	{
		final AbstractServerPlugin plugin = ServerPluginProvider.getInstance().getPlugins().stream().filter(currentPlugin -> currentPlugin.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
		if (plugin != null)
		{
			if (plugin.install())
			{
				PluginsTable.getInstance().addPlugin(plugin.getName(), plugin.getVersion());
				activeChar.sendMessage("Plugin: " + plugin.getName() + " has been installed succesfully!");
			}
			else
			{
				activeChar.sendMessage("Plugin: " + plugin.getName() + " has been installed unsuccesfully!");
			}
		}
	}
	
	/**
	 * @param activeChar
	 * @param pluginName
	 */
	private void uninstallPlugin(PlayerInstance activeChar, String pluginName)
	{
		final AbstractServerPlugin plugin = ServerPluginProvider.getInstance().getPlugins().stream().filter(currentPlugin -> currentPlugin.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
		if (plugin != null)
		{
			if (plugin.uninstall())
			{
				PluginsTable.getInstance().removePlugin(plugin.getName(), plugin.getVersion());
				plugin.onShutdown();
				activeChar.sendMessage("Plugin: " + plugin.getName() + " has been uninstalled succesfully!");
			}
			else
			{
				activeChar.sendMessage("Plugin: " + plugin.getName() + " has been ininstalled unsuccesfully!");
			}
		}
	}
	
	/**
	 * @param activeChar
	 * @param pluginName
	 */
	private void startPlugin(PlayerInstance activeChar, String pluginName)
	{
		final AbstractServerPlugin plugin = ServerPluginProvider.getInstance().getPlugins().stream().filter(currentPlugin -> currentPlugin.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
		if (plugin != null)
		{
			plugin.onInit();
			plugin.onLoad();
			plugin.onStart();
			activeChar.sendMessage("Plugin: " + plugin.getName() + " has been started succesfully!");
		}
	}
	
	/**
	 * @param activeChar
	 * @param pluginName
	 */
	private void shutdownPlugin(PlayerInstance activeChar, String pluginName)
	{
		final AbstractServerPlugin plugin = ServerPluginProvider.getInstance().getPlugins().stream().filter(currentPlugin -> currentPlugin.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
		if (plugin != null)
		{
			plugin.onShutdown();
			activeChar.sendMessage("Plugin: " + plugin.getName() + " has been shutted down succesfully!");
		}
	}
	
	/**
	 * @param activeChar
	 * @param pluginName
	 */
	private void reloadPlugin(PlayerInstance activeChar, String pluginName)
	{
		final AbstractServerPlugin plugin = ServerPluginProvider.getInstance().getPlugins().stream().filter(currentPlugin -> currentPlugin.getName().equalsIgnoreCase(pluginName)).findAny().orElse(null);
		if (plugin != null)
		{
			plugin.onReload();
			activeChar.sendMessage("Plugin: " + plugin.getName() + " has been reloaded succesfully!");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return COMMANDS;
	}
}
