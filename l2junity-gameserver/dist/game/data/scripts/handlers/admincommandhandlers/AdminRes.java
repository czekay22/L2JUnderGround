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

import org.l2junity.Config;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2ControllableMobInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.DecayTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles following admin commands: - res = resurrects target L2Character
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminRes implements IAdminCommandHandler
{
	private static Logger _log = LoggerFactory.getLogger(AdminRes.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_res",
		"admin_res_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("admin_res "))
		{
			handleRes(activeChar, command.split(" ")[1]);
		}
		else if (command.equals("admin_res"))
		{
			handleRes(activeChar);
		}
		else if (command.startsWith("admin_res_monster "))
		{
			handleNonPlayerRes(activeChar, command.split(" ")[1]);
		}
		else if (command.equals("admin_res_monster"))
		{
			handleNonPlayerRes(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRes(PlayerInstance activeChar)
	{
		handleRes(activeChar, null);
	}
	
	private void handleRes(PlayerInstance activeChar, String resParam)
	{
		WorldObject obj = activeChar.getTarget();
		
		if (resParam != null)
		{
			// Check if a player name was specified as a param.
			PlayerInstance plyr = World.getInstance().getPlayer(resParam);
			
			if (plyr != null)
			{
				obj = plyr;
			}
			else
			{
				// Otherwise, check if the param was a radius.
				try
				{
					int radius = Integer.parseInt(resParam);
					
					World.getInstance().forEachVisibleObjectInRange(activeChar, PlayerInstance.class, radius, knownPlayer ->
					{
						doResurrect(knownPlayer);
					});
					
					activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
					return;
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("Enter a valid player name or radius.");
					return;
				}
			}
		}
		
		if (obj == null)
		{
			obj = activeChar;
		}
		
		if (obj instanceof L2ControllableMobInstance)
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		doResurrect((Creature) obj);
		
		if (Config.DEBUG)
		{
			_log.debug("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
		}
	}
	
	private void handleNonPlayerRes(PlayerInstance activeChar)
	{
		handleNonPlayerRes(activeChar, "");
	}
	
	private void handleNonPlayerRes(PlayerInstance activeChar, String radiusStr)
	{
		WorldObject obj = activeChar.getTarget();
		
		try
		{
			int radius = 0;
			
			if (!radiusStr.isEmpty())
			{
				radius = Integer.parseInt(radiusStr);
				
				World.getInstance().forEachVisibleObjectInRange(activeChar, Creature.class, radius, knownChar ->
				{
					if (!(knownChar instanceof PlayerInstance) && !(knownChar instanceof L2ControllableMobInstance))
					{
						doResurrect(knownChar);
					}
				});
				
				activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
			}
		}
		catch (NumberFormatException e)
		{
			activeChar.sendMessage("Enter a valid radius.");
			return;
		}
		
		if ((obj instanceof PlayerInstance) || (obj instanceof L2ControllableMobInstance))
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		doResurrect((Creature) obj);
	}
	
	private void doResurrect(Creature targetChar)
	{
		if (!targetChar.isDead())
		{
			return;
		}
		
		// If the target is a player, then restore the XP lost on death.
		if (targetChar instanceof PlayerInstance)
		{
			((PlayerInstance) targetChar).restoreExp(100.0);
		}
		else
		{
			DecayTaskManager.getInstance().cancel(targetChar);
		}
		
		targetChar.doRevive();
	}
}
