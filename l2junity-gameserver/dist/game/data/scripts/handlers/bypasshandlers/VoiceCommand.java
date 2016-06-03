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
package handlers.bypasshandlers;

import org.l2junity.gameserver.handler.IBypassHandler;
import org.l2junity.gameserver.handler.IVoicedCommandHandler;
import org.l2junity.gameserver.handler.VoicedCommandHandler;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author DS
 */
public class VoiceCommand implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"voice"
	};
	
	@Override
	public boolean useBypass(String command, PlayerInstance activeChar, Creature target)
	{
		// only voice commands allowed
		if ((command.length() > 7) && (command.charAt(6) == '.'))
		{
			final String vc, vparams;
			int endOfCommand = command.indexOf(" ", 7);
			if (endOfCommand > 0)
			{
				vc = command.substring(7, endOfCommand).trim();
				vparams = command.substring(endOfCommand).trim();
			}
			else
			{
				vc = command.substring(7).trim();
				vparams = null;
			}
			
			if (vc.length() > 0)
			{
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(vc);
				if (vch != null)
				{
					return vch.useVoicedCommand(vc, activeChar, vparams);
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
