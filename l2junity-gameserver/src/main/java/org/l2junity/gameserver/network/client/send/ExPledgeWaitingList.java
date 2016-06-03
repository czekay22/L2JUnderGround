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

import java.util.Map;

import org.l2junity.gameserver.instancemanager.ClanEntryManager;
import org.l2junity.gameserver.model.clan.entry.PledgeApplicantInfo;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExPledgeWaitingList implements IClientOutgoingPacket
{
	private final Map<Integer, PledgeApplicantInfo> pledgePlayerRecruitInfos;
	
	public ExPledgeWaitingList(int clanId)
	{
		pledgePlayerRecruitInfos = ClanEntryManager.getInstance().getApplicantListForClan(clanId);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_WAITING_LIST.writeId(packet);
		
		packet.writeD(pledgePlayerRecruitInfos.size());
		for (PledgeApplicantInfo recruitInfo : pledgePlayerRecruitInfos.values())
		{
			packet.writeD(recruitInfo.getPlayerId());
			packet.writeS(recruitInfo.getPlayerName());
			packet.writeD(recruitInfo.getClassId());
			packet.writeD(recruitInfo.getPlayerLvl());
		}
		return true;
	}
}
