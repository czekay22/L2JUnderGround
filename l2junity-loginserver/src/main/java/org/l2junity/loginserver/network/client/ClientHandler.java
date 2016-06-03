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
package org.l2junity.loginserver.network.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.l2junity.loginserver.db.dto.Account;
import org.l2junity.loginserver.manager.LoginManager;
import org.l2junity.loginserver.network.client.crypt.KeyManager;
import org.l2junity.loginserver.network.client.crypt.ScrambledRSAKeyPair;
import org.l2junity.loginserver.network.client.send.Init;
import org.l2junity.network.ChannelInboundHandler;
import org.l2junity.network.IIncomingPacket;
import org.l2junity.network.IOutgoingPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NosBit
 */
public class ClientHandler extends ChannelInboundHandler<ClientHandler>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
	
	private InetAddress _address;
	private int _connectionId;
	private final SecretKey _blowfishKey;
	private final ScrambledRSAKeyPair _scrambledRSAKeyPair;
	private long _sessionId;
	private Channel _channel;
	private byte[] _gameGuard;
	private Account _account;
	private long _accountLoginsId;
	
	public ClientHandler(SecretKey blowfishKey)
	{
		super();
		_blowfishKey = blowfishKey;
		_scrambledRSAKeyPair = KeyManager.getInstance().getRandomScrambledRSAKeyPair();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		super.channelActive(ctx);
		
		setConnectionState(ConnectionState.CONNECTED);
		InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		_address = address.getAddress();
		_channel = ctx.channel();
		_connectionId = LoginManager.getInstance().getNextConnectionId();
		sendPacket(new Init(_connectionId, _scrambledRSAKeyPair, _blowfishKey));
		
		LOGGER.info("Client Connected: " + ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		LOGGER.info("Client Disconnected: " + ctx.channel());
	}
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, IIncomingPacket<ClientHandler> packet)
	{
		LOGGER.info(packet.getClass().getSimpleName() + " packet from: " + ctx.channel());
		try
		{
			packet.run(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		cause.printStackTrace();
	}
	
	public void sendPacket(IOutgoingPacket packet)
	{
		_channel.writeAndFlush(packet);
	}
	
	public void close(IOutgoingPacket packet)
	{
		setConnectionState(ConnectionState.CLOSED);
		_channel.writeAndFlush(packet);
		_channel.close();
	}
	
	/**
	 * Connection id
	 */
	
	/**
	 * @param id the connection id
	 */
	public void setConnectionId(int id)
	{
		_connectionId = id;
	}
	
	/**
	 * @return the connection id
	 */
	public int getConnectionId()
	{
		return _connectionId;
	}
	
	/**
	 * @return the scrambled RSA key pair
	 */
	public ScrambledRSAKeyPair getScrambledRSAKeyPair()
	{
		return _scrambledRSAKeyPair;
	}
	
	/**
	 * Session id
	 */
	
	/**
	 * @param id
	 */
	public void setLoginSessionId(long id)
	{
		_sessionId = id;
	}
	
	/**
	 * @return the session id
	 */
	public long getLoginSessionId()
	{
		return _sessionId;
	}
	
	/**
	 * Game Guard
	 */
	
	/**
	 * @param data
	 * @return
	 */
	public boolean checkGameGuard(byte[] data)
	{
		return Arrays.equals(_gameGuard, data);
	}
	
	/**
	 * @param data
	 */
	public void setGameGuard(byte[] data)
	{
		_gameGuard = data;
	}
	
	/**
	 * @return
	 */
	public InetAddress getInetAddress()
	{
		return _address;
	}
	
	public Account getAccount()
	{
		return _account;
	}
	
	public void setAccount(Account account)
	{
		_account = account;
	}
	
	public long getAccountLoginsId()
	{
		return _accountLoginsId;
	}
	
	public void setAccountLoginsId(long accountLoginsId)
	{
		_accountLoginsId = accountLoginsId;
	}
	
}
