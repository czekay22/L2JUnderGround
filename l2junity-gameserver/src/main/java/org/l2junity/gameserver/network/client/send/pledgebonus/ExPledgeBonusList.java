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
package org.l2junity.gameserver.network.client.send.pledgebonus;

import java.util.Comparator;

import org.l2junity.gameserver.data.xml.impl.ClanRewardData;
import org.l2junity.gameserver.enums.ClanRewardType;
import org.l2junity.gameserver.model.pledge.ClanRewardBonus;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class ExPledgeBonusList implements IClientOutgoingPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ExPledgeBonusList.class);
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_BONUS_LIST.writeId(packet);
		for (ClanRewardType type : ClanRewardType.values())
		{
			ClanRewardData.getInstance().getClanRewardBonuses(type).stream().sorted(Comparator.comparingInt(ClanRewardBonus::getLevel)).forEach(bonus ->
			{
				switch (type)
				{
					case MEMBERS_ONLINE:
					{
						if (bonus.getSkillReward() == null)
						{
							LOGGER.warn("Missing clan reward skill for reward level: {}", bonus.getLevel());
							packet.writeD(0);
							return;
						}
						
						packet.writeD(bonus.getSkillReward().getSkillId());
						break;
					}
					case HUNTING_MONSTERS:
					{
						if (bonus.getItemReward() == null)
						{
							LOGGER.warn("Missing clan reward skill for reward level: {}", bonus.getLevel());
							packet.writeD(0);
							return;
						}
						
						packet.writeD(bonus.getItemReward().getId());
						break;
					}
				}
			});
		}
		return true;
	}
}
