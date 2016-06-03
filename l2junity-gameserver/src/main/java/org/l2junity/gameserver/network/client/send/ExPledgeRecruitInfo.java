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

import org.l2junity.gameserver.data.sql.impl.ClanTable;
import org.l2junity.gameserver.instancemanager.ClanEntryManager;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.clan.entry.PledgeRecruitInfo;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExPledgeRecruitInfo implements IClientOutgoingPacket
{
	private final PledgeRecruitInfo _pledgeRecruitInfo;
	private final L2Clan _clan;
	
	public ExPledgeRecruitInfo(int clanId)
	{
		_pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(clanId);
		_clan = ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_RECRUIT_INFO.writeId(packet);
		
		if (_pledgeRecruitInfo == null)
		{
			packet.writeS(_clan.getName());
			packet.writeS(_clan.getLeaderName());
			packet.writeD(_clan.getLevel());
			packet.writeD(_clan.getMembersCount());
			packet.writeD(0x00);
		}
		else
		{
			packet.writeS(_pledgeRecruitInfo.getClan().getName());
			packet.writeS(_pledgeRecruitInfo.getClan().getLeaderName());
			packet.writeD(_pledgeRecruitInfo.getClan().getLevel());
			packet.writeD(_pledgeRecruitInfo.getClan().getMembersCount());
			packet.writeD(_pledgeRecruitInfo.getKarma());
		}
		return true;
	}
}
