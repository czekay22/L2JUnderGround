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

import org.l2junity.Config;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class PledgeShowInfoUpdate implements IClientOutgoingPacket
{
	private final L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SHOW_INFO_UPDATE.writeId(packet);
		
		// sending empty data so client will ask all the info in response ;)
		packet.writeD(_clan.getId());
		packet.writeD(Config.SERVER_ID);
		packet.writeD(_clan.getCrestId());
		packet.writeD(_clan.getLevel()); // clan level
		packet.writeD(_clan.getCastleId());
		packet.writeD(0x00); // castle state ?
		packet.writeD(_clan.getHideoutId());
		packet.writeD(_clan.getFortId());
		packet.writeD(_clan.getRank());
		packet.writeD(_clan.getReputationScore()); // clan reputation score
		packet.writeD(0x00); // ?
		packet.writeD(0x00); // ?
		packet.writeD(_clan.getAllyId());
		packet.writeS(_clan.getAllyName()); // c5
		packet.writeD(_clan.getAllyCrestId()); // c5
		packet.writeD(_clan.isAtWar() ? 1 : 0); // c5
		packet.writeD(0x00); // TODO: Find me!
		packet.writeD(0x00); // TODO: Find me!
		return true;
	}
}
