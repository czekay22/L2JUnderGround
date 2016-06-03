/*
 * Copyright (C) 2004-2014 L2J Unity
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
package org.l2junity.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteOrder;

import org.l2junity.network.IOutgoingPacket;
import org.l2junity.network.PacketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nos
 */
@Sharable
public class PacketEncoder extends MessageToByteEncoder<IOutgoingPacket>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketEncoder.class);
	
	private final ByteOrder _byteOrder;
	private final int _maxPacketSize;
	
	public PacketEncoder(ByteOrder byteOrder, int maxPacketSize)
	{
		super();
		_byteOrder = byteOrder;
		_maxPacketSize = maxPacketSize;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, IOutgoingPacket packet, ByteBuf out)
	{
		if (out.order() != _byteOrder)
		{
			out = out.order(_byteOrder);
		}

		try
		{
			if (packet.write(new PacketWriter(out)))
			{
				if (out.writerIndex() > _maxPacketSize)
				{
					throw new IllegalStateException("Packet (" + packet + ") size (" + out.writerIndex() + ") is bigger than the limit (" + _maxPacketSize + ")");
				}
			}
			else
			{
				// Avoid sending the packet
				out.clear();
			}
		}
		catch (Throwable e)
		{
			LOGGER.warn("Failed sending Packet({})", packet, e);
			// Avoid sending the packet if some exception happened
			out.clear();
		}
	}
}