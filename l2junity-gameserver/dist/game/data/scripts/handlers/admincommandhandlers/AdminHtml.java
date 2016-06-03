/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;

import org.l2junity.Config;
import org.l2junity.gameserver.cache.HtmCache;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;

/**
 * @author NosBit
 */
public class AdminHtml implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_html",
		"admin_loadhtml"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		switch (actualCommand.toLowerCase())
		{
			case "admin_html":
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Usage: //html path");
					return false;
				}
				
				final String path = st.nextToken();
				showAdminHtml(activeChar, path);
				break;
			}
			case "admin_loadhtml":
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Usage: //loadhtml path");
					return false;
				}
				
				final String path = st.nextToken();
				showHtml(activeChar, path, true);
				break;
			}
		}
		return true;
	}
	
	/**
	 * Shows a html message to activeChar
	 * @param activeChar activeChar where html is shown
	 * @param path relative path from directory data/html/admin/ to html
	 */
	public static void showAdminHtml(PlayerInstance activeChar, String path)
	{
		showHtml(activeChar, "data/html/admin/" + path, false);
	}
	
	/**
	 * Shows a html message to activeChar.
	 * @param activeChar activeChar where html message is shown.
	 * @param path relative path from Config.DATAPACK_ROOT to html.
	 * @param reload {@code true} will reload html and show it {@code false} will show it from cache.
	 */
	public static void showHtml(PlayerInstance activeChar, String path, boolean reload)
	{
		String content = null;
		if (!reload)
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), path);
		}
		else
		{
			content = HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path));
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
		if (content != null)
		{
			html.setHtml(content);
		}
		else
		{
			html.setHtml("<html><body>My text is missing:<br>" + path + "</body></html>");
		}
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		
		return ADMIN_COMMANDS;
	}
	
}
