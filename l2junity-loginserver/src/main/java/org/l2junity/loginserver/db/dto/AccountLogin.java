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
public class AccountLogin
{
	private final long _id;
	private final long _accountId;
	private short _serverId;
	private final String _ip;
	private final Instant _loggedInAt;
	
	/**
	 * Creates an account login instance.
	 * @param id the id
	 * @param accountId the account id
	 * @param serverId the server id
	 * @param ip the ip
	 * @param loggedInAt the logged in at
	 */
	public AccountLogin(long id, long accountId, short serverId, String ip, Instant loggedInAt)
	{
		_id = id;
		_accountId = accountId;
		_serverId = serverId;
		_ip = ip;
		_loggedInAt = loggedInAt;
	}
	
	/**
	 * Gets the id.
	 * @return the id
	 */
	public long getId()
	{
		return _id;
	}
	
	/**
	 * Gets the account id
	 * @return the account id
	 */
	public long getAccountId()
	{
		return _accountId;
	}
	
	/**
	 * Gets the server id.
	 * @return the server id
	 */
	public short getServerId()
	{
		return _serverId;
	}
	
	/**
	 * Sets the server id
	 * @param serverId the server id to set
	 */
	public void setServerId(short serverId)
	{
		_serverId = serverId;
	}
	
	/**
	 * Gets the IP.
	 * @return the IP
	 */
	public String getIp()
	{
		return _ip;
	}
	
	/**
	 * Gets the logged in at.
	 * @return the logged in at
	 */
	public Instant getLoggedInAt()
	{
		return _loggedInAt;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", "AccountLogin[", "]");
		sj.add("id: " + _id);
		sj.add("account id: " + _accountId);
		sj.add("server id: " + _serverId);
		sj.add("ip: " + _ip);
		sj.add("logged in at: " + _loggedInAt);
		return sj.toString();
	}
}
