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
package org.l2junity.gameserver.network.client.recv;

import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExBrLoadEventTopRankers;
import org.l2junity.network.PacketReader;

/**
 * Halloween rank list client packet. Format: (ch)ddd
 */
public class BrEventRankerList implements IClientIncomingPacket
{
	private int _eventId;
	private int _day;
	@SuppressWarnings("unused")
	private int _ranking;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_eventId = packet.readD();
		_day = packet.readD(); // 0 - current, 1 - previous
		_ranking = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		// TODO count, bestScore, myScore
		int count = 0;
		int bestScore = 0;
		int myScore = 0;
		client.sendPacket(new ExBrLoadEventTopRankers(_eventId, _day, count, bestScore, myScore));
	}
}
