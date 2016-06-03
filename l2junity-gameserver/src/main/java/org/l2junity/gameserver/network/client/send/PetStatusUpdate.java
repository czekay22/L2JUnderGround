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
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.L2ServitorInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * This class ...
 * @version $Revision: 1.5.2.3.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusUpdate implements IClientOutgoingPacket
{
	private final Summon _summon;
	private int _maxFed, _curFed;
	
	public PetStatusUpdate(Summon summon)
	{
		_summon = summon;
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (_summon instanceof L2ServitorInstance)
		{
			L2ServitorInstance sum = (L2ServitorInstance) _summon;
			_curFed = sum.getLifeTimeRemaining();
			_maxFed = sum.getLifeTime();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PET_STATUS_UPDATE.writeId(packet);
		
		packet.writeD(_summon.getSummonType());
		packet.writeD(_summon.getObjectId());
		packet.writeD(_summon.getX());
		packet.writeD(_summon.getY());
		packet.writeD(_summon.getZ());
		packet.writeS(_summon.getTitle());
		packet.writeD(_curFed);
		packet.writeD(_maxFed);
		packet.writeD((int) _summon.getCurrentHp());
		packet.writeD(_summon.getMaxHp());
		packet.writeD((int) _summon.getCurrentMp());
		packet.writeD(_summon.getMaxMp());
		packet.writeD(_summon.getLevel());
		packet.writeQ(_summon.getStat().getExp());
		packet.writeQ(_summon.getExpForThisLevel()); // 0% absolute value
		packet.writeQ(_summon.getExpForNextLevel()); // 100% absolute value
		packet.writeD(0x01); // TODO: Find me!
		return true;
	}
}
