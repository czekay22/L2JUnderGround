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
package handlers.telnethandlers.player;

import io.netty.channel.ChannelHandlerContext;

import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;
import org.l2junity.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class Give implements ITelnetCommand
{
	private Give()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "give";
	}
	
	@Override
	public String getUsage()
	{
		return "Give <player name> <item id> [item amount] [item enchant] [donators]";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		if ((args.length < 2) || args[0].isEmpty() || !Util.isDigit(args[1]))
		{
			return null;
		}
		final PlayerInstance player = World.getInstance().getPlayer(args[0]);
		if (player != null)
		{
			int itemId = Integer.parseInt(args[1]);
			long amount = 1;
			int enchanted = 0;
			if (args.length > 2)
			{
				String token = args[2];
				if (Util.isDigit(token))
				{
					amount = Long.parseLong(token);
				}
				if (args.length > 3)
				{
					token = args[3];
					if (Util.isDigit(token))
					{
						enchanted = Integer.parseInt(token);
					}
				}
			}
			
			final ItemInstance item = ItemTable.getInstance().createItem("Telnet-Admin", itemId, amount, player, null);
			if (enchanted > 0)
			{
				item.setEnchantLevel(enchanted);
			}
			player.addItem("Telnet-Admin", item, null, true);
			return "Item has been successfully given to the player.";
		}
		return "Couldn't find player with such name.";
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new Give());
	}
}
