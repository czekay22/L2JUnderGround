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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author KenM
 */
public class ExDuelUpdateUserInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	
	public ExDuelUpdateUserInfo(PlayerInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_DUEL_UPDATE_USER_INFO.writeId(packet);
		
		packet.writeS(_activeChar.getName());
		packet.writeD(_activeChar.getObjectId());
		packet.writeD(_activeChar.getClassId().getId());
		packet.writeD(_activeChar.getLevel());
		packet.writeD((int) _activeChar.getCurrentHp());
		packet.writeD(_activeChar.getMaxHp());
		packet.writeD((int) _activeChar.getCurrentMp());
		packet.writeD(_activeChar.getMaxMp());
		packet.writeD((int) _activeChar.getCurrentCp());
		packet.writeD(_activeChar.getMaxCp());
		return true;
	}
}
