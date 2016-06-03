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

import java.util.StringJoiner;

/**
 * @author NosBit
 */
public class AccountOTP
{
	private final long _id;
	private final long _accountId;
	private final String _name;
	private final String _code;
	
	/**
	 * Creates an account OTP instance.
	 * @param id the id
	 * @param accountId the account id
	 * @param name the name
	 * @param code the code
	 */
	public AccountOTP(long id, long accountId, String name, String code)
	{
		_id = id;
		_accountId = accountId;
		_name = name;
		_code = code;
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
	 * Gets the account id.
	 * @return the account id
	 */
	public long getAccountId()
	{
		return _accountId;
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
	 * Gets the code.
	 * @return the code
	 */
	public String getCode()
	{
		return _code;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", "AccountOTP[", "]");
		sj.add("id: " + _id);
		sj.add("account id: " + _accountId);
		sj.add("name: " + _name);
		sj.add("code: " + _code);
		return sj.toString();
	}
}
