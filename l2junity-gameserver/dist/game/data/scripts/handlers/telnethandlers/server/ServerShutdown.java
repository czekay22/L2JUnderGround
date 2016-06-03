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
package handlers.telnethandlers.server;

import io.netty.channel.ChannelHandlerContext;

import org.l2junity.gameserver.Shutdown;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;
import org.l2junity.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class ServerShutdown implements ITelnetCommand
{
	private ServerShutdown()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "shutdown";
	}
	
	@Override
	public String getUsage()
	{
		return "Shutdown <time in seconds>";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		if ((args.length == 0) || !Util.isDigit(args[0]))
		{
			return null;
		}
		int time = Integer.parseInt(args[0]);
		Shutdown.getInstance().startTelnetShutdown(ctx.channel().remoteAddress().toString(), time, false);
		return "Server will shutdown in " + time + " seconds!";
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new ServerShutdown());
	}
}
