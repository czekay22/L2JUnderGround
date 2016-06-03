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

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author KenM
 */
public class ExDuelReady implements IClientOutgoingPacket
{
	public static final ExDuelReady PLAYER_DUEL = new ExDuelReady(false);
	public static final ExDuelReady PARTY_DUEL = new ExDuelReady(true);
	
	private final int _partyDuel;
	
	public ExDuelReady(boolean isPartyDuel)
	{
		_partyDuel = isPartyDuel ? 1 : 0;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_DUEL_READY.writeId(packet);
		
		packet.writeD(_partyDuel);
		return true;
	}
}
