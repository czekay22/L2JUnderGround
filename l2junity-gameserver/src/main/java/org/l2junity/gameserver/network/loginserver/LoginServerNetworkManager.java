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
package org.l2junity.gameserver.network.loginserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.l2junity.Config;
import org.l2junity.gameserver.network.EventLoopGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NosBit
 */
public class LoginServerNetworkManager
{
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final Bootstrap _bootstrap;

	private ChannelFuture _channelFuture;

	public LoginServerNetworkManager()
	{
		//@formatter:off
		_bootstrap = new Bootstrap()
			.group(EventLoopGroupManager.getInstance().getWorkerGroup())
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(new LoginServerInitializer());
		//@formatter:on
	}

	public ChannelFuture getChannelFuture()
	{
		return _channelFuture;
	}

	public void connect() throws InterruptedException
	{
		if (_channelFuture != null && _channelFuture.isSuccess())
		{
			return;
		}
		_channelFuture = _bootstrap.connect(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT).sync();
		LOGGER.info("Connected to {}:{}", Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
	}

	public void disconnect() throws InterruptedException
	{
		_channelFuture.channel().close().sync();
	}

	public static LoginServerNetworkManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final LoginServerNetworkManager _instance = new LoginServerNetworkManager();
	}
}
