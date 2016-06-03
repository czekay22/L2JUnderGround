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

import org.l2junity.gameserver.data.xml.impl.ClanHallData;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.entity.ClanHall;
import org.l2junity.gameserver.model.zone.ZoneId;

/**
 * A clan hall zone
 * @author durgus
 */
public class ClanHallZone extends ResidenceZone
{
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			setResidenceId(Integer.parseInt(value));
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.CLAN_HALL, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.CLAN_HALL, false);
		}
	}
	
	@Override
	public final Location getBanishSpawnLoc()
	{
		final ClanHall clanHall = ClanHallData.getInstance().getClanHallById(getResidenceId());
		if (clanHall == null)
		{
			return null;
		}
		return clanHall.getBanishLocation();
	}
}