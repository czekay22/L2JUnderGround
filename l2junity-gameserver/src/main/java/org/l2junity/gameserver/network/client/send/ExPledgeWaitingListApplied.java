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

import org.l2junity.gameserver.instancemanager.ClanEntryManager;
import org.l2junity.gameserver.model.clan.entry.PledgeApplicantInfo;
import org.l2junity.gameserver.model.clan.entry.PledgeRecruitInfo;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExPledgeWaitingListApplied implements IClientOutgoingPacket
{
	private final PledgeApplicantInfo _pledgePlayerRecruitInfo;
	private final PledgeRecruitInfo _pledgeRecruitInfo;
	
	public ExPledgeWaitingListApplied(int clanId, int playerId)
	{
		_pledgePlayerRecruitInfo = ClanEntryManager.getInstance().getPlayerApplication(clanId, playerId);
		_pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(clanId);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_WAITING_LIST_APPLIED.writeId(packet);
		
		packet.writeD(_pledgeRecruitInfo.getClan().getId());
		packet.writeS(_pledgeRecruitInfo.getClan().getName());
		packet.writeS(_pledgeRecruitInfo.getClan().getLeaderName());
		packet.writeD(_pledgeRecruitInfo.getClan().getLevel());
		packet.writeD(_pledgeRecruitInfo.getClan().getMembersCount());
		packet.writeD(_pledgeRecruitInfo.getKarma());
		packet.writeS(_pledgeRecruitInfo.getInformation());
		packet.writeS(_pledgePlayerRecruitInfo.getMessage());
		return true;
	}
}
