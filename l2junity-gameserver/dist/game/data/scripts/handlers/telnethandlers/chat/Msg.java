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

import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;

/**
 * @author UnAfraid
 */
public class Msg implements ITelnetCommand
{
	private Msg()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "msg";
	}
	
	@Override
	public String getUsage()
	{
		return "Msg <player> <text>";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		if ((args.length < 2) || args[0].isEmpty())
		{
			return null;
		}
		final PlayerInstance player = World.getInstance().getPlayer(args[0]);
		if (player != null)
		{
			final StringBuilder sb = new StringBuilder();
			for (int i = 1; i < args.length; i++)
			{
				sb.append(args[i] + " ");
			}
			player.sendPacket(new CreatureSay(0, ChatType.WHISPER, "Telnet Priv", sb.toString()));
			return "Announcement sent!";
		}
		return "Couldn't find player with such name.";
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new Msg());
	}
}
