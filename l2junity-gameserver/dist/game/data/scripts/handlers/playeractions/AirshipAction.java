/*
 * Copyright (C) 2004-2015 L2J Unity
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
package handlers.playeractions;

import org.l2junity.gameserver.handler.IPlayerActionHandler;
import org.l2junity.gameserver.handler.PlayerActionHandler;
import org.l2junity.gameserver.instancemanager.AirShipManager;
import org.l2junity.gameserver.model.ActionDataHolder;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

/**
 * Airship Action player action handler.
 * @author Nik
 */
public final class AirshipAction implements IPlayerActionHandler
{
	@Override
	public void useAction(PlayerInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		if (!activeChar.isInAirShip())
		{
			return;
		}
		
		switch (data.getOptionId())
		{
			case 1: // Steer
			{
				if (activeChar.getAirShip().setCaptain(activeChar))
				{
					activeChar.broadcastUserInfo();
				}
				break;
			}
			case 2: // Cancel Control
			{
				if (activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			}
			case 3: // Destination Map
			{
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			}
			case 4: // Exit Airship
			{
				if (activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				else if (activeChar.getAirShip().isInDock())
				{
					activeChar.getAirShip().oustPlayer(activeChar);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		PlayerActionHandler.getInstance().registerHandler(new AirshipAction());
	}
}
