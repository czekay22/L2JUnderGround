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
public class AccountBan
{
	private final long _id;
	private final long _accountId;
	private boolean _active;
	private final Instant _startedAt;
	private final Instant _expiresAt;
	private final String _reason;
	
	/**
	 * Creates an account ban instance.
	 * @param id the id
	 * @param accountId the account id
	 * @param active the active
	 * @param startedAt the started at
	 * @param expiresAt the expires at
	 * @param reason the reason
	 */
	public AccountBan(long id, long accountId, boolean active, Instant startedAt, Instant expiresAt, String reason)
	{
		super();
		_id = id;
		_accountId = accountId;
		_active = active;
		_startedAt = startedAt;
		_expiresAt = expiresAt;
		_reason = reason;
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
	 * @return the account id
	 */
	public long getAccountId()
	{
		return _accountId;
	}
	
	/**
	 * Checks if is active.
	 * @return the active
	 */
	public boolean isActive()
	{
		return _active;
	}
	
	/**
	 * Sets if is active.
	 * @param active the active to set
	 */
	public void setActive(boolean active)
	{
		_active = active;
	}
	
	/**
	 * Gets the started at.
	 * @return the started at
	 */
	public Instant getStartedAt()
	{
		return _startedAt;
	}
	
	/**
	 * Gets the expires at.
	 * @return the expires at
	 */
	public Instant getExpiresAt()
	{
		return _expiresAt;
	}
	
	/**
	 * Gets the reason.
	 * @return the reason
	 */
	public String getReason()
	{
		return _reason;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", "AccountBan[", "]");
		sj.add("id: " + _id);
		sj.add("account id: " + _accountId);
		sj.add("active: " + _active);
		sj.add("started at: " + _startedAt);
		sj.add("expires at:" + _expiresAt);
		sj.add("reason:" + _reason);
		return sj.toString();
	}
}
