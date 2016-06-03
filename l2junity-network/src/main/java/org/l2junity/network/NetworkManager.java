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
package org.l2junity.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nos
 */
public class NetworkManager
{
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private final ServerBootstrap _serverBootstrap;
	private final String _host;
	private final int _port;
	
	private ChannelFuture _channelFuture;
	
	public NetworkManager(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ChannelInitializer<SocketChannel> clientInitializer, String host, int port)
	{
		// @formatter:off
		_serverBootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(clientInitializer)
			.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// @formatter:on
		_host = host;
		_port = port;
	}
	
	public ChannelFuture getChannelFuture()
	{
		return _channelFuture;
	}
	
	public void start() throws InterruptedException
	{
		if ((_channelFuture != null) && !_channelFuture.isDone())
		{
			return;
		}
		
		_channelFuture = _serverBootstrap.bind(_host, _port).sync();
		LOGGER.info("Listening on {}:{}", _host, _port);
	}
	
	public void stop() throws InterruptedException
	{
		_channelFuture.channel().close().sync();
	}
}
