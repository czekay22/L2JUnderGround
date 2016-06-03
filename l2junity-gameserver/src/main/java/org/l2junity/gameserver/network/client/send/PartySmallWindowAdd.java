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

import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class PartySmallWindowAdd implements IClientOutgoingPacket
{
	private final PlayerInstance _member;
	private final Party _party;
	
	public PartySmallWindowAdd(PlayerInstance member, Party party)
	{
		_member = member;
		_party = party;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_ADD.writeId(packet);
		
		packet.writeD(_party.getLeaderObjectId()); // c3
		packet.writeD(_party.getDistributionType().getId()); // c3
		packet.writeD(_member.getObjectId());
		packet.writeS(_member.getName());
		
		packet.writeD((int) _member.getCurrentCp()); // c4
		packet.writeD(_member.getMaxCp()); // c4
		packet.writeD((int) _member.getCurrentHp());
		packet.writeD(_member.getMaxHp());
		packet.writeD((int) _member.getCurrentMp());
		packet.writeD(_member.getMaxMp());
		packet.writeD(_member.getVitalityPoints());
		packet.writeC(_member.getLevel());
		packet.writeH(_member.getClassId().getId());
		packet.writeC(0x00);
		packet.writeH(_member.getRace().ordinal());
		return true;
	}
}
