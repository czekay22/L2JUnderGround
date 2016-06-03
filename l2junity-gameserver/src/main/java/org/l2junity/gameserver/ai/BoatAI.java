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
package org.l2junity.gameserver.ai;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.L2BoatInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.VehicleDeparture;
import org.l2junity.gameserver.network.client.send.VehicleInfo;
import org.l2junity.gameserver.network.client.send.VehicleStarted;

/**
 * @author DS
 */
public class BoatAI extends VehicleAI
{
	public BoatAI(L2BoatInstance boat)
	{
		super(boat);
	}
	
	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			if (!_clientMoving)
			{
				_actor.broadcastPacket(new VehicleStarted(getActor(), 1));
			}
			
			_clientMoving = true;
			_actor.moveToLocation(x, y, z, 0);
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	public void clientStopMoving(Location loc)
	{
		if (_actor.isMoving())
		{
			_actor.stopMove(loc);
		}
		
		if (_clientMoving || (loc != null))
		{
			_clientMoving = false;
			_actor.broadcastPacket(new VehicleStarted(getActor(), 0));
			_actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}
	
	@Override
	public void describeStateToPlayer(PlayerInstance player)
	{
		if (_clientMoving)
		{
			player.sendPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	public L2BoatInstance getActor()
	{
		return (L2BoatInstance) _actor;
	}
}