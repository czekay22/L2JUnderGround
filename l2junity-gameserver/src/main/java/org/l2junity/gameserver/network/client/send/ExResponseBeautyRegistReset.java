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
 * @author Sdw
 */
public class ExResponseBeautyRegistReset implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final int _type;
	private final int _result;
	
	public final static int FAILURE = 0;
	public final static int SUCCESS = 1;
	
	public final static int CHANGE = 0;
	public final static int RESTORE = 1;
	
	public ExResponseBeautyRegistReset(PlayerInstance activeChar, int type, int result)
	{
		_activeChar = activeChar;
		_type = type;
		_result = result;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RESPONSE_BEAUTY_REGIST_RESET.writeId(packet);
		
		packet.writeQ(_activeChar.getAdena());
		packet.writeQ(_activeChar.getBeautyTickets());
		packet.writeD(_type);
		packet.writeD(_result);
		packet.writeD(_activeChar.getVisualHair());
		packet.writeD(_activeChar.getVisualFace());
		packet.writeD(_activeChar.getVisualHairColor());
		return true;
	}
}
