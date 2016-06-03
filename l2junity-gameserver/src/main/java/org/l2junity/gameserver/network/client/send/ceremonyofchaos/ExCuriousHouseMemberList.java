/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.network.client.send.ceremonyofchaos;

import java.util.Collection;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ceremonyofchaos.CeremonyOfChaosMember;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public class ExCuriousHouseMemberList implements IClientOutgoingPacket
{
	private final int _id;
	private final int _maxPlayers;
	private final Collection<CeremonyOfChaosMember> _players;
	
	public ExCuriousHouseMemberList(int id, int maxPlayers, Collection<CeremonyOfChaosMember> players)
	{
		_id = id;
		_maxPlayers = maxPlayers;
		_players = players;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CURIOUS_HOUSE_MEMBER_LIST.writeId(packet);
		
		packet.writeD(_id);
		packet.writeD(_maxPlayers);
		packet.writeD(_players.size());
		for (CeremonyOfChaosMember cocPlayer : _players)
		{
			final PlayerInstance player = cocPlayer.getPlayer();
			packet.writeD(cocPlayer.getObjectId());
			packet.writeD(cocPlayer.getPosition());
			if (player != null)
			{
				packet.writeD(player.getMaxHp());
				packet.writeD(player.getMaxCp());
				packet.writeD((int) player.getCurrentHp());
				packet.writeD((int) player.getCurrentCp());
			}
			else
			{
				packet.writeD(0x00);
				packet.writeD(0x00);
				packet.writeD(0x00);
				packet.writeD(0x00);
			}
		}
		return true;
	}
	
}
