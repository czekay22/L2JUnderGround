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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Nos
 * @param <T>
 */
public abstract class ChannelInboundHandler<T extends ChannelInboundHandler<?>> extends SimpleChannelInboundHandler<IIncomingPacket<T>>
{
	private Channel _channel;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		_channel = ctx.channel();
	}
	
	public void setConnectionState(IConnectionState connectionState)
	{
		_channel.attr(IConnectionState.ATTRIBUTE_KEY).set(connectionState);
	}
	
	public IConnectionState getConnectionState()
	{
		return _channel != null ? _channel.attr(IConnectionState.ATTRIBUTE_KEY).get() : null;
	}
}
