/*
 * Copyright (C) 2004-2013 L2J Unity
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
package handlers.telnethandlers.chat;

import io.netty.channel.ChannelHandlerContext;

import org.l2junity.gameserver.data.xml.impl.AdminData;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;

/**
 * @author UnAfraid
 */
public class GMChat implements ITelnetCommand
{
	private GMChat()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "gmchat";
	}
	
	@Override
	public String getUsage()
	{
		return "Gmchat <text>";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		if ((args.length == 0) || args[0].isEmpty())
		{
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		for (String str : args)
		{
			sb.append(str + " ");
		}
		AdminData.getInstance().broadcastToGMs(new CreatureSay(0, ChatType.ALLIANCE, "Telnet GM Broadcast", sb.toString()));
		return "GMChat sent!";
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new GMChat());
	}
}
