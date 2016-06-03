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
package org.l2junity.loginserver.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.l2junity.loginserver.db.dto.AccountBan;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * @author NosBit
 */
public class AccountBanMapper implements ResultSetMapper<AccountBan>
{
	@Override
	public AccountBan map(int index, ResultSet r, StatementContext ctx) throws SQLException
	{
		return new AccountBan(r.getLong("id"), r.getLong("account_id"), r.getBoolean("active"), r.getTimestamp("started_at").toInstant(), r.getTimestamp("expires_at").toInstant(), r.getString("reason"));
	}
}
