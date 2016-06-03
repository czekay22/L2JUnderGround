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
package org.l2junity.gameserver.network;

import io.netty.channel.nio.NioEventLoopGroup;

import org.l2junity.Config;

/**
 * @author Nos
 */
public class EventLoopGroupManager
{
	private final NioEventLoopGroup _bossGroup = new NioEventLoopGroup(1);
	private final NioEventLoopGroup _workerGroup = new NioEventLoopGroup(Config.IO_PACKET_THREAD_CORE_SIZE);
	
	public NioEventLoopGroup getBossGroup()
	{
		return _bossGroup;
	}
	
	public NioEventLoopGroup getWorkerGroup()
	{
		return _workerGroup;
	}
	
	public void shutdown()
	{
		_bossGroup.shutdownGracefully();
		_workerGroup.shutdownGracefully();
	}
	
	public static EventLoopGroupManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventLoopGroupManager _instance = new EventLoopGroupManager();
	}
}
