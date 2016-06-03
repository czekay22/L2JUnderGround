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

import org.l2junity.gameserver.model.TeleportBookmark;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author ShanSoft
 */
public class ExGetBookMarkInfoPacket implements IClientOutgoingPacket
{
	private final PlayerInstance player;
	
	public ExGetBookMarkInfoPacket(PlayerInstance cha)
	{
		player = cha;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_GET_BOOK_MARK_INFO.writeId(packet);
		
		packet.writeD(0x00); // Dummy
		packet.writeD(player.getBookmarkslot());
		packet.writeD(player.getTeleportBookmarks().size());
		
		for (TeleportBookmark tpbm : player.getTeleportBookmarks())
		{
			packet.writeD(tpbm.getId());
			packet.writeD(tpbm.getX());
			packet.writeD(tpbm.getY());
			packet.writeD(tpbm.getZ());
			packet.writeS(tpbm.getName());
			packet.writeD(tpbm.getIcon());
			packet.writeS(tpbm.getTag());
		}
		return true;
	}
}
