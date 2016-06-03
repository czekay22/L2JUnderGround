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
package org.l2junity.gameserver.model.zone.type;

import java.util.concurrent.ScheduledFuture;

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

/**
 * Teleport residence zone for clan hall sieges
 * @author BiggBoss
 */
public class ResidenceHallTeleportZone extends ResidenceTeleportZone
{
	private int _id;
	private ScheduledFuture<?> _teleTask;
	
	/**
	 * @param id
	 */
	public ResidenceHallTeleportZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("residenceZoneId"))
		{
			_id = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	public int getResidenceZoneId()
	{
		return _id;
	}
	
	public synchronized void checkTeleporTask()
	{
		if ((_teleTask == null) || _teleTask.isDone())
		{
			_teleTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), 30000);
		}
	}
	
	protected class TeleportTask implements Runnable
	{
		@Override
		public void run()
		{
			int index = 0;
			if (getSpawns().size() > 1)
			{
				index = Rnd.get(getSpawns().size());
			}
			
			final Location loc = getSpawns().get(index);
			if (loc == null)
			{
				throw new NullPointerException();
			}
			
			for (PlayerInstance pc : getPlayersInside())
			{
				if (pc != null)
				{
					pc.teleToLocation(loc, false);
				}
			}
		}
	}
}
