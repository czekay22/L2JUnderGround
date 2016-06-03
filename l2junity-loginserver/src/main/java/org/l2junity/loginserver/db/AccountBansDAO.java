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
package org.l2junity.loginserver.db;

import java.io.Closeable;
import java.sql.Timestamp;
import java.util.List;

import org.l2junity.loginserver.db.dto.Account;
import org.l2junity.loginserver.db.dto.AccountBan;
import org.l2junity.loginserver.db.mapper.AccountBanMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * @author NosBit
 */
@RegisterMapper(AccountBanMapper.class)
public interface AccountBansDAO extends Closeable
{
	@SqlUpdate("INSERT INTO `account_bans`(`account_id`, `expires_at`, `reason`) VALUES(:accountId, :expiresAt, :reason)")
	long insert(@Bind("accountId") long accountId, @Bind("expiresAt") Timestamp expiresAt, @Bind("reason") String reason);
	
	@SqlUpdate("UPDATE `account_bans` SET `active` = :active WHERE `id` = :id")
	int updateActive(@Bind("id") long id, @Bind("active") boolean active);
	
	@SqlQuery("SELECT * FROM `account_bans` WHERE `account_id` = :accountId")
	List<AccountBan> findByAccountId(@Bind("accountId") long accountId);
	
	@SqlQuery("SELECT * FROM `account_bans` WHERE `account_id` = :id")
	List<AccountBan> findByAccountId(@BindBean Account account);
	
	@SqlQuery("SELECT * FROM `account_bans` WHERE `active` = TRUE AND `expires_at` > CURRENT_TIMESTAMP AND `account_id` = :accountId")
	List<AccountBan> findActiveByAccountId(@Bind("accountId") long accountId);
	
	@SqlQuery("SELECT * FROM `account_bans` WHERE `active` = TRUE AND `expires_at` > CURRENT_TIMESTAMP AND `account_id` = :id")
	List<AccountBan> findActiveByAccountId(@BindBean Account account);
	
	@Override
	void close();
}
