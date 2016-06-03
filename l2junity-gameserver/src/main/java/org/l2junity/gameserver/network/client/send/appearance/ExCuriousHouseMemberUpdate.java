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
package org.l2junity.gameserver.network.client.send.appearance;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ceremonyofchaos.CeremonyOfChaosMember;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExCuriousHouseMemberUpdate implements IClientOutgoingPacket
{
	public final int _objId;
	public final int _maxHp;
	public final int _maxCp;
	public final int _currentHp;
	public final int _currentCp;
	
	public ExCuriousHouseMemberUpdate(CeremonyOfChaosMember member)
	{
		_objId = member.getObjectId();
		final PlayerInstance player = member.getPlayer();
		if (player != null)
		{
			_maxHp = player.getMaxHp();
			_maxCp = player.getMaxCp();
			_currentHp = (int) player.getCurrentHp();
			_currentCp = (int) player.getCurrentCp();
		}
		else
		{
			_maxHp = 0;
			_maxCp = 0;
			_currentHp = 0;
			_currentCp = 0;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CURIOUS_HOUSE_MEMBER_UPDATE.writeId(packet);
		
		packet.writeD(_objId);
		packet.writeD(_maxHp);
		packet.writeD(_maxCp);
		packet.writeD(_currentHp);
		packet.writeD(_currentCp);
		return true;
	}
}
