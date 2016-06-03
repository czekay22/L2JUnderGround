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
package org.l2junity.loginserver;

import java.sql.SQLException;

import org.l2junity.loginserver.db.AccountBansDAO;
import org.l2junity.loginserver.db.AccountLoginsDAO;
import org.l2junity.loginserver.db.AccountOTPsDAO;
import org.l2junity.loginserver.db.AccountsDAO;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This class manages the database connections.
 */
public class DatabaseFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFactory.class);
	
	private final ComboPooledDataSource _source = new ComboPooledDataSource();
	private final DBI _dbi;
	
	/**
	 * Creates a database factory instance.
	 */
	public DatabaseFactory()
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				LOGGER.warn("A minimum of {} db connections are required.", Config.DATABASE_MAX_CONNECTIONS);
			}
			
			_source.setAutoCommitOnClose(true);
			
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.
			
			// this "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);
			
			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
			
			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour
			
			// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);
			
			_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass("com.mysql.jdbc.Driver");
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);
			
			/* Test the connection */
			_source.getConnection().close();
			LOGGER.info("Connected!");
		}
		catch (Exception e)
		{
			LOGGER.error("Could not init DB connection:", e);
			System.exit(0);
		}
		finally
		{
			_dbi = new DBI(_source);
		}
	}
	
	/**
	 * Shutdown.
	 */
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			LOGGER.info("", e);
		}
	}
	
	/**
	 * Gets the handle.
	 * @return the handle
	 * @throws DBIException
	 */
	public Handle getHandle() throws DBIException
	{
		Handle handle = null;
		while (handle == null)
		{
			try
			{
				handle = _dbi.open();
			}
			catch (UnableToObtainConnectionException e)
			{
				LOGGER.warn("getHandle() failed, trying again", e);
			}
		}
		return handle;
	}
	
	public <R> R withHandle(HandleCallback<R> callback) throws DBIException
	{
		return _dbi.withHandle(callback);
	}
	
	public AccountsDAO getAccountsDAO() throws DBIException
	{
		return _dbi.open(AccountsDAO.class);
	}
	
	public AccountOTPsDAO getAccountOTPsDAO() throws DBIException
	{
		return _dbi.open(AccountOTPsDAO.class);
	}
	
	public AccountBansDAO getAccountBansDAO() throws DBIException
	{
		return _dbi.open(AccountBansDAO.class);
	}
	
	public AccountLoginsDAO getAccountLoginsDAO() throws DBIException
	{
		return _dbi.open(AccountLoginsDAO.class);
	}
	
	/**
	 * Gets the busy connection count.
	 * @return the busy connection count
	 * @throws SQLException the SQL exception
	 */
	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}
	
	/**
	 * Gets the idle connection count.
	 * @return the idle connection count
	 * @throws SQLException the SQL exception
	 */
	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}
	
	public static DatabaseFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final class SingletonHolder
	{
		protected static final DatabaseFactory _instance = new DatabaseFactory();
	}
}
