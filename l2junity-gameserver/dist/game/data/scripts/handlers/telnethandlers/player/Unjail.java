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

import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.instancemanager.PunishmentManager;
import org.l2junity.gameserver.model.punishment.PunishmentAffect;
import org.l2junity.gameserver.model.punishment.PunishmentType;
import org.l2junity.gameserver.network.telnet.ITelnetCommand;
import org.l2junity.gameserver.network.telnet.TelnetServer;

/**
 * @author UnAfraid
 */
public class Unjail implements ITelnetCommand
{
	@Override
	public String getCommand()
	{
		return "unjail";
	}
	
	@Override
	public String getUsage()
	{
		return "Unjail <player name>";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		if ((args.length == 0) || args[0].isEmpty())
		{
			return null;
		}
		int objectId = CharNameTable.getInstance().getIdByName(args[0]);
		if (objectId > 0)
		{
			if (!PunishmentManager.getInstance().hasPunishment(objectId, PunishmentAffect.CHARACTER, PunishmentType.JAIL))
			{
				return "Player is not jailed at all.";
			}
			PunishmentManager.getInstance().stopPunishment(objectId, PunishmentAffect.CHARACTER, PunishmentType.JAIL);
			return "Player has been successfully unjailed.";
		}
		return "Couldn't find player with such name.";
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new Unjail());
	}
}
