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
package org.l2junity.gameserver.network.client.send;

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExShowUsm implements IClientOutgoingPacket
{
	public static final ExShowUsm GOD_INTRO = new ExShowUsm(2);
	public static final ExShowUsm SECOND_TRANSFER_QUEST = new ExShowUsm(4);
	public static final ExShowUsm OCTAVIS_INSTANCE_END = new ExShowUsm(6);
	public static final ExShowUsm AWAKENING_END = new ExShowUsm(10);
	public static final ExShowUsm ERTHEIA_FIRST_QUEST = new ExShowUsm(14);
	public static final ExShowUsm ERTHEIA_INTRO_FOR_ERTHEIA = new ExShowUsm(147);
	public static final ExShowUsm ERTHEIA_INTRO_FOR_OTHERS = new ExShowUsm(148);
	
	private final int _videoId;
	
	private ExShowUsm(int videoId)
	{
		_videoId = videoId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_USM.writeId(packet);
		
		packet.writeD(_videoId);
		return true;
	}
}
