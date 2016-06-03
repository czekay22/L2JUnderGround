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
import java.util.List;

import org.l2junity.loginserver.db.dto.Account;
import org.l2junity.loginserver.db.dto.AccountOTP;
import org.l2junity.loginserver.db.mapper.AccountOTPMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * @author NosBit
 */
@RegisterMapper(AccountOTPMapper.class)
public interface AccountOTPsDAO extends Closeable
{
	@SqlUpdate("INSERT INTO `account_otps`(`account_id`, `name`, `code`) VALUES(:accountId, :name, :code)")
	@GetGeneratedKeys
	long insert(@Bind("accountId") long accountId, @Bind("name") String name, @Bind("code") String code);
	
	@SqlUpdate("DELETE FROM `account_otps` WHERE `id` = :id")
	int delete(@Bind("id") long id);
	
	@SqlUpdate("DELETE FROM `account_otps` WHERE `id` = :id")
	int delete(@BindBean AccountOTP accountOTP);
	
	@SqlQuery("SELECT * FROM `account_otps` WHERE `account_id` = :accountId")
	List<AccountOTP> findByAccountId(@Bind("accountId") long accountId);
	
	@SqlQuery("SELECT * FROM `account_otps` WHERE `account_id` = :id")
	List<AccountOTP> findByAccountId(@BindBean Account account);
	
	@Override
	void close();
}
