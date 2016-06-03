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
package org.l2junity.gameserver.network.telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.l2junity.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class TelnetServer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TelnetServer.class);
	private final Map<String, ITelnetCommand> _commands = new LinkedHashMap<>();
	private final EventLoopGroup _workerGroup = new NioEventLoopGroup(1);
	
	protected TelnetServer()
	{
		if (Config.TELNET_ENABLED)
		{
			init();
		}
		else
		{
			LOGGER.info("Telnet server is currently disabled.");
		}
	}
	
	public void init()
	{
		addHandler(new ITelnetCommand()
		{
			@Override
			public String getCommand()
			{
				return "help";
			}
			
			@Override
			public String getUsage()
			{
				return "help [command]";
			}
			
			@Override
			public String handle(ChannelHandlerContext ctx, String[] args)
			{
				if (args.length == 0)
				{
					final StringBuilder sb = new StringBuilder("Available commands:" + System.lineSeparator());
					for (ITelnetCommand cmd : TelnetServer.getInstance().getCommands())
					{
						sb.append(cmd.getCommand() + System.lineSeparator());
					}
					return sb.toString();
				}
				final ITelnetCommand cmd = TelnetServer.getInstance().getCommand(args[0]);
				if (cmd == null)
				{
					return "Unknown command." + System.lineSeparator();
				}
				return "Usage:" + System.lineSeparator() + cmd.getUsage() + System.lineSeparator();
			}
		});
		
		try
		{
			final InetSocketAddress socket = Config.TELNET_HOSTNAME.equals("*") ? new InetSocketAddress(Config.TELNET_PORT) : new InetSocketAddress(Config.TELNET_HOSTNAME, Config.TELNET_PORT);
			//@formatter:off
			new ServerBootstrap().group(_workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new TelnetServerInitializer())
				.bind(socket);
			//@formatter:on
			LOGGER.info("Listening on " + Config.TELNET_HOSTNAME + ":" + Config.TELNET_PORT);
		}
		catch (Exception e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
	}
	
	public void addHandler(ITelnetCommand handler)
	{
		_commands.put(handler.getCommand(), handler);
	}
	
	public ITelnetCommand getCommand(String command)
	{
		return _commands.get(command);
	}
	
	public Collection<ITelnetCommand> getCommands()
	{
		return _commands.values();
	}
	
	public void shutdown()
	{
		_workerGroup.shutdownGracefully();
		LOGGER.info("Shutting down..");
	}
	
	public static TelnetServer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TelnetServer _instance = new TelnetServer();
	}
}
