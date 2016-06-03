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
package org.l2junity.loginserver.network.gameserver;

import org.l2junity.loginserver.Config;
import org.l2junity.loginserver.network.EventLoopGroupManager;
import org.l2junity.network.NetworkManager;

/**
 * @author NosBit
 */
public class GameServerNetworkManager extends NetworkManager
{
	protected GameServerNetworkManager()
	{
		super(EventLoopGroupManager.getInstance().getBossGroup(), EventLoopGroupManager.getInstance().getWorkerGroup(), new GameServerInitializer(), Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
	}
	
	public static GameServerNetworkManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerNetworkManager _instance = new GameServerNetworkManager();
	}
}
