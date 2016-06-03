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
package org.l2junity.loginserver.model.enums;

/**
 * An {@code enum} representing the server types such as Relax, Test, Broad, Create Restrict, Event, Free, World Raid, New and Classic.
 * @author Nos
 */
public enum ServerType
{
	RELAX(0x02),
	TEST(0x04),
	BROAD(0x08),
	CREATE_RESTRICT(0x10),
	EVENT(0x20),
	FREE(0x40),
	WORLD_RAID(0x100),
	NEW(0x200),
	CLASSIC(0x400);
	
	private final int _mask;
	
	/**
	 * Creates a server type instance.
	 * @param mask the mask
	 */
	ServerType(int mask)
	{
		_mask = mask;
	}
	
	/**
	 * Gets the mask.
	 * @return the mask
	 */
	public int getMask()
	{
		return _mask;
	}
}
