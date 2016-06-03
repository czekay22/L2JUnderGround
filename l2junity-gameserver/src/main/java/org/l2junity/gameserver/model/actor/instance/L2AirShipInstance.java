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
package org.l2junity.gameserver.model.actor.instance;

import org.l2junity.gameserver.ai.AirShipAI;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.instancemanager.AirShipManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Vehicle;
import org.l2junity.gameserver.model.actor.templates.L2CharTemplate;
import org.l2junity.gameserver.network.client.send.ExAirShipInfo;
import org.l2junity.gameserver.network.client.send.ExGetOffAirShip;
import org.l2junity.gameserver.network.client.send.ExGetOnAirShip;
import org.l2junity.gameserver.network.client.send.ExMoveToLocationAirShip;
import org.l2junity.gameserver.network.client.send.ExStopMoveAirShip;

/**
 * Flying airships. Very similar to Maktakien boats (see L2BoatInstance) but these do fly :P
 * @author DrHouse, DS
 */
public class L2AirShipInstance extends Vehicle
{
	public L2AirShipInstance(L2CharTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2AirShipInstance);
		setAI(new AirShipAI(this));
	}
	
	@Override
	public boolean isAirShip()
	{
		return true;
	}
	
	public boolean isOwner(PlayerInstance player)
	{
		return false;
	}
	
	public int getOwnerId()
	{
		return 0;
	}
	
	public boolean isCaptain(PlayerInstance player)
	{
		return false;
	}
	
	public int getCaptainId()
	{
		return 0;
	}
	
	public int getHelmObjectId()
	{
		return 0;
	}
	
	public int getHelmItemId()
	{
		return 0;
	}
	
	public boolean setCaptain(PlayerInstance player)
	{
		return false;
	}
	
	public int getFuel()
	{
		return 0;
	}
	
	public void setFuel(int f)
	{
		
	}
	
	public int getMaxFuel()
	{
		return 0;
	}
	
	public void setMaxFuel(int mf)
	{
		
	}
	
	@Override
	public int getId()
	{
		return 0;
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			broadcastPacket(new ExMoveToLocationAirShip(this));
		}
		
		return result;
	}
	
	@Override
	public boolean addPassenger(PlayerInstance player)
	{
		if (!super.addPassenger(player))
		{
			return false;
		}
		
		player.setVehicle(this);
		player.setInVehiclePosition(new Location(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}
	
	@Override
	public void oustPlayer(PlayerInstance player)
	{
		super.oustPlayer(player);
		final Location loc = getOustLoc();
		if (player.isOnline())
		{
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
		}
	}
	
	@Override
	public boolean deleteMe()
	{
		if (!super.deleteMe())
		{
			return false;
		}
		
		AirShipManager.getInstance().removeAirShip(this);
		return true;
	}
	
	@Override
	public void stopMove(Location loc)
	{
		super.stopMove(loc);
		
		broadcastPacket(new ExStopMoveAirShip(this));
	}
	
	@Override
	public void updateAbnormalVisualEffects()
	{
		broadcastPacket(new ExAirShipInfo(this));
	}
	
	@Override
	public void sendInfo(PlayerInstance activeChar)
	{
		if (isVisibleFor(activeChar))
		{
			activeChar.sendPacket(new ExAirShipInfo(this));
		}
	}
}