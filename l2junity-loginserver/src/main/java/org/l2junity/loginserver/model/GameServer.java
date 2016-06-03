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
package org.l2junity.loginserver.model;

import java.util.Set;

import org.l2junity.loginserver.model.enums.AgeLimit;
import org.l2junity.loginserver.model.enums.ServerType;

/**
 * @author NosBit
 */
public class GameServer
{
	private final short _id;
	private final String _name;
	private final boolean _showing;
	private final AgeLimit _ageLimit;
	private final Set<ServerType> _serverTypes;
	
	/**
	 * Creates a new game server instance.
	 * @param id the id
	 * @param name the name
	 * @param showing the showing
	 * @param ageLimit the age limit
	 * @param serverTypes the server types
	 */
	public GameServer(short id, String name, boolean showing, AgeLimit ageLimit, Set<ServerType> serverTypes)
	{
		if ((id < 0) && (id > 0xFF))
		{
			throw new IllegalStateException("GameServer id should be between 0 and 255");
		}
		_id = id;
		_name = name;
		_showing = showing;
		_ageLimit = ageLimit;
		_serverTypes = serverTypes;
	}
	
	/**
	 * Gets the id.
	 * @return the id
	 */
	public short getId()
	{
		return _id;
	}
	
	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Checks if is showing.
	 * @return the showing
	 */
	public boolean isShowing()
	{
		return _showing;
	}
	
	/**
	 * Gets the age limit.
	 * @return the age limit
	 */
	public AgeLimit getAgeLimit()
	{
		return _ageLimit;
	}
	
	/**
	 * Gets the server types.
	 * @return the server types
	 */
	public Set<ServerType> getServerTypes()
	{
		return _serverTypes;
	}
	
	/**
	 * Gets the server types mask.
	 * @return the server types mask
	 */
	public int getServerTypesMask()
	{
		return _serverTypes.stream().mapToInt(ServerType::getMask).reduce((r, e) -> r | e).orElse(0);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if ((o == null) || (getClass() != o.getClass()))
		{
			return false;
		}
		
		GameServer that = (GameServer) o;
		
		return _id == that._id;
		
	}
	
	@Override
	public int hashCode()
	{
		return _id;
	}
	
	@Override
	public String toString()
	{
		//@formatter:off
		return "GameServer{" +
			"id=" + _id +
			", name='" + _name + '\'' +
			", showing=" + _showing +
			", ageLimit=" + _ageLimit +
			", serverTypes=" + _serverTypes +
			'}';
		//@formatter:on
	}
}
