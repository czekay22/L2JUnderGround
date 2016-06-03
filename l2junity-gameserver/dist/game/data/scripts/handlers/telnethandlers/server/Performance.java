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

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;

/**
 * @author UnAfraid
 */
public class Performance implements ITelnetCommand
{
	private Performance()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "performance";
	}
	
	@Override
	public String getUsage()
	{
		return "Performance";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		ThreadPoolManager.getInstance().purge();
		final StringBuilder sb = new StringBuilder();
		for (String line : ThreadPoolManager.getInstance().getStats())
		{
			sb.append(line + System.lineSeparator());
		}
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new Performance());
	}
}
