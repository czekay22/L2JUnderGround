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
package org.l2junity.loginserver.db.dto;

import java.time.Instant;
import java.util.StringJoiner;

/**
 * @author NosBit
 */
public class Account
{
	private final long _id;
	private final String _name;
	private String _password;
	private short _lastServerId;
	private final Instant _createdAt;
	
	/**
	 * Creates an account instance.
	 * @param id the id
	 * @param name the name
	 * @param password the password
	 * @param lastServerId the last server id
	 * @param createdAt the created at
	 */
	public Account(long id, String name, String password, short lastServerId, Instant createdAt)
	{
		_id = id;
		_name = name;
		_password = password;
		_lastServerId = lastServerId;
		_createdAt = createdAt;
	}
	
	/**
	 * Gets the id
	 * @return the id
	 */
	public long getId()
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
	 * Gets the password.
	 * @return the password
	 */
	public String getPassword()
	{
		return _password;
	}
	
	/**
	 * Sets the password.
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		_password = password;
	}
	
	/**
	 * Gets the last server id.
	 * @return the last server id
	 */
	public short getLastServerId()
	{
		return _lastServerId;
	}
	
	/**
	 * Sets the last server id.
	 * @param lastServerId the last server id to set
	 */
	public void setLastServerId(short lastServerId)
	{
		_lastServerId = lastServerId;
	}
	
	/**
	 * Gets the created at.
	 * @return the created at
	 */
	public Instant getCreatedAt()
	{
		return _createdAt;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", "Account[", "]");
		sj.add("id: " + _id);
		sj.add("name: " + _name);
		sj.add("password: " + _password);
		sj.add("last server id: " + _lastServerId);
		sj.add("created at:" + _createdAt);
		return sj.toString();
	}
}
