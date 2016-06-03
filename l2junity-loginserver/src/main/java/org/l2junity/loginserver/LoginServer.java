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



import org.l2junity.loginserver.manager.GameServerManager;
import org.l2junity.loginserver.network.client.ClientNetworkManager;
import org.l2junity.loginserver.network.client.crypt.KeyManager;
import org.l2junity.loginserver.network.gameserver.GameServerNetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NosBit
 */
public class LoginServer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginServer.class);
	
	private LoginServer()
	{
		try
		{
			printSection("Config");
			Config.load();
			
			printSection("Database");
			DatabaseFactory.getInstance();

			printSection("Data");
			GameServerManager.getInstance();
			
			printSection("Network");
			KeyManager.getInstance();
			GameServerNetworkManager.getInstance().start();
			ClientNetworkManager.getInstance().start();
		}
		catch (Exception e)
		{
			LOGGER.warn("Error while initializing: ", e);
		}

		try
		{
			GameServerNetworkManager.getInstance().getChannelFuture().channel().closeFuture().sync();
			ClientNetworkManager.getInstance().getChannelFuture().channel().closeFuture().sync();
		}
		catch (InterruptedException e)
		{
			LOGGER.warn("", e);
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 65 - LoginServer.class.getSimpleName().length())
		{
			s = "-" + s;
		}
		LOGGER.info(s);
	}
	
	public static void main(String[] args)
	{
		new LoginServer();
	}
}
