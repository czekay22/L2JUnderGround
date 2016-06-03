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

import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author KenM
 */
public class ExPartyPetWindowUpdate implements IClientOutgoingPacket
{
	private final Summon _summon;
	
	public ExPartyPetWindowUpdate(Summon summon)
	{
		_summon = summon;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PARTY_PET_WINDOW_UPDATE.writeId(packet);
		
		packet.writeD(_summon.getObjectId());
		packet.writeD(_summon.getTemplate().getDisplayId() + 1000000);
		packet.writeC(_summon.getSummonType());
		packet.writeD(_summon.getOwner().getObjectId());
		packet.writeD((int) _summon.getCurrentHp());
		packet.writeD(_summon.getMaxHp());
		packet.writeD((int) _summon.getCurrentMp());
		packet.writeD(_summon.getMaxMp());
		return true;
	}
}
