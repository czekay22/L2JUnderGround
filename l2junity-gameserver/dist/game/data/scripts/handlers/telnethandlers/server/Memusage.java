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

import java.text.DecimalFormat;

import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;

/**
 * @author UnAfraid
 */
public class Memusage implements ITelnetCommand
{
	private Memusage()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "memusage";
	}
	
	@Override
	public String getUsage()
	{
		return "MemUsage";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		double max = Runtime.getRuntime().maxMemory() / 1024; // maxMemory is the upper
		// limit the jvm can use
		double allocated = Runtime.getRuntime().totalMemory() / 1024; // totalMemory the
		// size of the current allocation pool
		double nonAllocated = max - allocated; // non allocated memory till jvm limit
		double cached = Runtime.getRuntime().freeMemory() / 1024; // freeMemory the
		// unused memory in the allocation pool
		double used = allocated - cached; // really used memory
		double useable = max - used; // allocated, but non-used and non-allocated memory
		
		StringBuilder sb = new StringBuilder();
		
		DecimalFormat df = new DecimalFormat(" (0.0000'%')");
		DecimalFormat df2 = new DecimalFormat(" # 'KB'");
		
		sb.append("+----" + System.lineSeparator());// ...
		sb.append("| Allowed Memory:" + df2.format(max) + System.lineSeparator());
		sb.append("|    |= Allocated Memory:" + df2.format(allocated) + df.format((allocated / max) * 100) + System.lineSeparator());
		sb.append("|    |= Non-Allocated Memory:" + df2.format(nonAllocated) + df.format((nonAllocated / max) * 100) + System.lineSeparator());
		sb.append("| Allocated Memory:" + df2.format(allocated) + System.lineSeparator());
		sb.append("|    |= Used Memory:" + df2.format(used) + df.format((used / max) * 100) + System.lineSeparator());
		sb.append("|    |= Unused (cached) Memory:" + df2.format(cached) + df.format((cached / max) * 100) + System.lineSeparator());
		sb.append("| Useable Memory:" + df2.format(useable) + df.format((useable / max) * 100) + System.lineSeparator()); // ...
		sb.append("+----" + System.lineSeparator());
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new Memusage());
	}
}
