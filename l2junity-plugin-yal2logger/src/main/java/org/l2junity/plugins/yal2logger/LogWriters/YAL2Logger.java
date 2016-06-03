/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.plugins.yal2logger.LogWriters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.l2junity.Config;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class YAL2Logger implements IPacketHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(YAL2Logger.class);
	private static final String YAL2_PROTOCOL_NAME = "Infinite Odyssey";
	private static final byte YAL2_VERSION = 0x07;
	private final L2GameClient _client;
	private final AtomicInteger _packets = new AtomicInteger();
	private RandomAccessFile _randomAccessFile;
	private final File _file;
	
	public YAL2Logger(L2GameClient client)
	{
		_client = client;
		final File curDir = new File("log/packetlogs");
		if (!curDir.exists())
		{
			curDir.mkdirs();
		}
		final LocalDateTime now = LocalDateTime.now();
		final String fileName = _client.getConnectionAddress().getHostAddress() + " [" + now.getDayOfWeek() + "-" + now.getMonthValue() + "-" + now.getYear() + "] (" + now.getHour() + "-" + now.getMinute() + "-" + now.getSecond() + ").l2l";
		_file = new File(curDir, fileName);
		try
		{
			_randomAccessFile = new RandomAccessFile(_file, "rw");
			writeYAL2Header();
		}
		catch (Exception e)
		{
			LOGGER.warn("Error while creating random accesss file:", e);
		}
	}
	
	private void writeYAL2Header() throws IOException
	{
		_randomAccessFile.writeByte(YAL2_VERSION);
		_randomAccessFile.writeInt(0x00); // Packets count
		_randomAccessFile.writeByte(0x00); // Split log
		
		_randomAccessFile.writeShort(Short.reverseBytes((short) 0x00)); // Client port
		_randomAccessFile.writeShort(Short.reverseBytes((short) Config.PORT_GAME)); // Server port
		
		_randomAccessFile.write(getClientIp()); // Client ip
		_randomAccessFile.write(getServerIp()); // Server ip
		
		writeS(YAL2_PROTOCOL_NAME, _randomAccessFile); // Protocol name
		writeS("Log sniffed from game server.", _randomAccessFile); // Comments
		writeS("L2J", _randomAccessFile); // Server Type
		
		_randomAccessFile.writeLong(0x00); // Analyser bit set
		_randomAccessFile.writeLong(0x00); // Session id
		_randomAccessFile.writeByte(0x00); // Is encrypted
	}
	
	@Override
	public synchronized void handlePacket(byte[] data, boolean clientSide)
	{
		try
		{
			_randomAccessFile.writeByte(!clientSide ? (byte) 0x01 : 0x00);
			_randomAccessFile.writeShort(Short.reverseBytes((short) (data.length + 2)));
			_randomAccessFile.writeLong(Long.reverseBytes(System.currentTimeMillis()));
			_randomAccessFile.write(data);
			
			final int packets = _packets.incrementAndGet();
			
			// Update packets
			final long pos = _randomAccessFile.getFilePointer();
			_randomAccessFile.seek(1);
			_randomAccessFile.writeInt(Integer.reverseBytes(packets));
			_randomAccessFile.seek(pos);
		}
		catch (Exception e)
		{
			LOGGER.warn("", e);
		}
	}
	
	@Override
	public void notifyTerminate()
	{
		final File curDir = new File("log/packetlogs/" + _client.getAccountName());
		if (!curDir.exists())
		{
			curDir.mkdirs();
		}
		try
		{
			Files.copy(_file.toPath(), new File(curDir, _file.getName()).toPath());
		}
		catch (IOException e)
		{
			LOGGER.warn("Couldn't copy the file: ", e);
		}
	}
	
	private static void writeS(String text, RandomAccessFile raf) throws IOException
	{
		if ((text != null) && !text.isEmpty())
		{
			final char[] chars = text.toCharArray();
			for (char c : chars)
			{
				raf.writeChar(Character.reverseBytes(c));
			}
		}
		raf.writeChar(0);
	}
	
	private byte[] getClientIp()
	{
		try
		{
			return _client.getConnectionAddress().getAddress();
		}
		catch (Exception e)
		{
			return new byte[4];
		}
	}
	
	private byte[] getServerIp()
	{
		try
		{
			return InetAddress.getLocalHost().getAddress();
		}
		catch (Exception e)
		{
			return new byte[4];
		}
	}
}
