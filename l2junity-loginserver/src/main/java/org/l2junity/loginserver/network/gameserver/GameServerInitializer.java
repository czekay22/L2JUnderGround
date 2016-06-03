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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.ByteOrder;

import javax.crypto.SecretKey;

import org.l2junity.loginserver.network.client.crypt.Crypt;
import org.l2junity.loginserver.network.client.crypt.KeyManager;
import org.l2junity.network.codecs.CryptCodec;
import org.l2junity.network.codecs.LengthFieldBasedFrameEncoder;
import org.l2junity.network.codecs.PacketDecoder;
import org.l2junity.network.codecs.PacketEncoder;

/**
 * @author NosBit
 */
public class GameServerInitializer extends ChannelInitializer<SocketChannel>
{
	private static final LengthFieldBasedFrameEncoder LENGTH_ENCODER = new LengthFieldBasedFrameEncoder();
	private static final PacketEncoder PACKET_ENCODER = new PacketEncoder(ByteOrder.LITTLE_ENDIAN, 0x8000 - 2);
	
	@Override
	protected void initChannel(SocketChannel ch)
	{
		final SecretKey blowfishKey = KeyManager.getInstance().generateBlowfishKey();
		ch.pipeline().addLast("length-decoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 0x8000 - 2, 0, 2, -2, 2, false));
		ch.pipeline().addLast("length-encoder", LENGTH_ENCODER);
		ch.pipeline().addLast("crypt-codec", new CryptCodec(new Crypt(blowfishKey)));
		ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));

		final GameServerHandler gameServerHandler = new GameServerHandler();
		ch.pipeline().addLast("packet-decoder", new PacketDecoder<>(ByteOrder.LITTLE_ENDIAN, IncomingPackets.PACKET_ARRAY, gameServerHandler));
		ch.pipeline().addLast("packet-encoder", PACKET_ENCODER);
		ch.pipeline().addLast(gameServerHandler);
	}
}
