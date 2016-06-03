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

import org.l2junity.gameserver.model.Shortcut;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ShortCutInit implements IClientOutgoingPacket
{
	private Shortcut[] _shortCuts;
	
	public ShortCutInit(PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		_shortCuts = activeChar.getAllShortCuts();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SHORT_CUT_INIT.writeId(packet);
		
		packet.writeD(_shortCuts.length);
		for (Shortcut sc : _shortCuts)
		{
			packet.writeD(sc.getType().ordinal());
			packet.writeD(sc.getSlot() + (sc.getPage() * 12));
			
			switch (sc.getType())
			{
				case ITEM:
				{
					packet.writeD(sc.getId());
					packet.writeD(0x01); // Enabled or not
					packet.writeD(sc.getSharedReuseGroup());
					packet.writeD(0x00);
					packet.writeD(0x00);
					packet.writeQ(0x00); // Augment id
					packet.writeD(0x00); // Visual id
					break;
				}
				case SKILL:
				{
					packet.writeD(sc.getId());
					packet.writeD(sc.getLevel());
					packet.writeD(sc.getSharedReuseGroup());
					packet.writeC(0x00); // C5
					packet.writeD(0x01); // C6
					break;
				}
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
				{
					packet.writeD(sc.getId());
					packet.writeD(0x01); // C6
				}
			}
		}
		return true;
	}
}
