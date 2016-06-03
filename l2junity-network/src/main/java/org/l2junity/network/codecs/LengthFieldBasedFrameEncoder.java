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
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.ByteOrder;
import java.util.List;

/**
 * @author Nos
 */
@Sharable
public class LengthFieldBasedFrameEncoder extends MessageToMessageEncoder<ByteBuf>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
	{
		final ByteBuf buf = ctx.alloc().buffer(2);
		final short length = (short) (msg.readableBytes() + 2);
		buf.writeShort(buf.order() != ByteOrder.LITTLE_ENDIAN ? Short.reverseBytes(length) : length);
		out.add(buf);
		out.add(msg.retain());
	}
}
