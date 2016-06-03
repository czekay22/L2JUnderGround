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
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class PartySmallWindowAll implements IClientOutgoingPacket
{
	private final Party _party;
	private final PlayerInstance _exclude;
	
	public PartySmallWindowAll(PlayerInstance exclude, Party party)
	{
		_exclude = exclude;
		_party = party;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_ALL.writeId(packet);
		
		packet.writeD(_party.getLeaderObjectId());
		packet.writeC(_party.getDistributionType().getId());
		packet.writeC(_party.getMemberCount() - 1);
		
		for (PlayerInstance member : _party.getMembers())
		{
			if ((member != null) && (member != _exclude))
			{
				packet.writeD(member.getObjectId());
				packet.writeS(member.getName());
				
				packet.writeD((int) member.getCurrentCp()); // c4
				packet.writeD(member.getMaxCp()); // c4
				
				packet.writeD((int) member.getCurrentHp());
				packet.writeD(member.getMaxHp());
				packet.writeD((int) member.getCurrentMp());
				packet.writeD(member.getMaxMp());
				packet.writeD(member.getVitalityPoints());
				packet.writeC(member.getLevel());
				packet.writeH(member.getClassId().getId());
				packet.writeC(0x01); // Unk
				packet.writeH(member.getRace().ordinal());
				final Summon pet = member.getPet();
				packet.writeD(member.getServitors().size() + (pet != null ? 1 : 0)); // Summon size, one only atm
				if (pet != null)
				{
					packet.writeD(pet.getObjectId());
					packet.writeD(pet.getId() + 1000000);
					packet.writeC(pet.getSummonType());
					packet.writeS(pet.getName());
					packet.writeD((int) pet.getCurrentHp());
					packet.writeD(pet.getMaxHp());
					packet.writeD((int) pet.getCurrentMp());
					packet.writeD(pet.getMaxMp());
					packet.writeC(pet.getLevel());
				}
				member.getServitors().values().forEach(s ->
				{
					packet.writeD(s.getObjectId());
					packet.writeD(s.getId() + 1000000);
					packet.writeC(s.getSummonType());
					packet.writeS(s.getName());
					packet.writeD((int) s.getCurrentHp());
					packet.writeD(s.getMaxHp());
					packet.writeD((int) s.getCurrentMp());
					packet.writeD(s.getMaxMp());
					packet.writeC(s.getLevel());
				});
			}
		}
		return true;
	}
}
