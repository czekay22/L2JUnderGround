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
public class ExShowBeautyMenu implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final int _type;
	
	// TODO: Enum
	public final static int MODIFY_APPEARANCE = 0;
	public final static int RESTORE_APPEARANCE = 1;
	
	public ExShowBeautyMenu(PlayerInstance activeChar, int type)
	{
		_activeChar = activeChar;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_BEAUTY_MENU.writeId(packet);
		
		packet.writeD(_type);
		packet.writeD(_activeChar.getVisualHair());
		packet.writeD(_activeChar.getVisualHairColor());
		packet.writeD(_activeChar.getVisualFace());
		return true;
	}
}