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

import java.awt.Color;

import org.l2junity.gameserver.data.xml.impl.DoorData;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.DoorInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Castle;
import org.l2junity.gameserver.network.client.send.ExServerPrimitive;

/**
 * This class handles following admin commands: - open1 = open coloseum door 24190001 - open2 = open coloseum door 24190002 - open3 = open coloseum door 24190003 - open4 = open coloseum door 24190004 - openall = open all coloseum door - close1 = close coloseum door 24190001 - close2 = close coloseum
 * door 24190002 - close3 = close coloseum door 24190003 - close4 = close coloseum door 24190004 - closeall = close all coloseum door - open = open selected door - close = close selected door
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static DoorData _doorTable = DoorData.getInstance();
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall",
		"admin_checkNodes",
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		try
		{
			if (command.startsWith("admin_checkNodes"))
			{
				if (command.equals("admin_checkNodes"))
				{
					final ExServerPrimitive exsp = new ExServerPrimitive("DebugPoint_" + activeChar.getObjectId(), activeChar.getX(), activeChar.getY(), activeChar.getZ());
					exsp.addPoint(Color.BLACK, 0, 0, 0);
					activeChar.sendPacket(exsp);
				}
				else
				{
					final int objId = Integer.parseInt(command.substring(17));
					final WorldObject target = World.getInstance().findObject(objId);
					if ((target != null) && target.isDoor())
					{
						final DoorInstance door = (DoorInstance) target;
						final ExServerPrimitive exsp = new ExServerPrimitive("DebugPoint_" + activeChar.getObjectId(), activeChar.getX(), activeChar.getY(), activeChar.getZ());
						
						exsp.addLine("", Color.RED, true, door.getX(0), door.getY(0), door.getZMin(), door.getX(1), door.getY(1), door.getZMin());
						exsp.addLine("", Color.RED, true, door.getX(1), door.getY(1), door.getZMin(), door.getX(2), door.getY(2), door.getZMin());
						exsp.addLine("", Color.RED, true, door.getX(2), door.getY(2), door.getZMax(), door.getX(3), door.getY(3), door.getZMax());
						exsp.addLine("", Color.RED, true, door.getX(3), door.getY(3), door.getZMin(), door.getX(0), door.getY(0), door.getZMax());
						activeChar.sendPacket(exsp);
					}
				}
			}
			else if (command.startsWith("admin_open "))
			{
				int doorId = Integer.parseInt(command.substring(11));
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).openMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
					}
				}
			}
			else if (command.startsWith("admin_close "))
			{
				int doorId = Integer.parseInt(command.substring(12));
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).closeMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
					}
				}
			}
			if (command.equals("admin_closeall"))
			{
				for (DoorInstance door : _doorTable.getDoors())
				{
					door.closeMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			if (command.equals("admin_openall"))
			{
				for (DoorInstance door : _doorTable.getDoors())
				{
					door.openMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			if (command.equals("admin_open"))
			{
				WorldObject target = activeChar.getTarget();
				if (target instanceof DoorInstance)
				{
					((DoorInstance) target).openMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}
			
			if (command.equals("admin_close"))
			{
				WorldObject target = activeChar.getTarget();
				if (target instanceof DoorInstance)
				{
					((DoorInstance) target).closeMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
