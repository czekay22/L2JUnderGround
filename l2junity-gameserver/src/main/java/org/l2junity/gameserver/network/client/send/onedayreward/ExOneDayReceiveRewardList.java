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
package org.l2junity.gameserver.network.client.send.onedayreward;

import java.time.LocalDate;
import java.util.Collection;

import org.l2junity.gameserver.data.xml.impl.OneDayRewardData;
import org.l2junity.gameserver.model.OneDayRewardDataHolder;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExOneDayReceiveRewardList implements IClientOutgoingPacket
{
	final PlayerInstance _player;
	private final Collection<OneDayRewardDataHolder> _rewards;
	
	public ExOneDayReceiveRewardList(PlayerInstance player)
	{
		_player = player;
		_rewards = OneDayRewardData.getInstance().getOneDayRewardData(player);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ONE_DAY_RECEIVE_REWARD_LIST.writeId(packet);
		
		packet.writeD(_player.getClassId().getId());
		packet.writeD(LocalDate.now().getDayOfWeek().ordinal()); // Day of week
		packet.writeD(_rewards.size());
		for (OneDayRewardDataHolder reward : _rewards)
		{
			packet.writeH(reward.getId());
			packet.writeC(reward.getStatus(_player));
			packet.writeC(reward.getRequiredCompletions() > 0 ? 0x01 : 0x00);
			packet.writeD(reward.getProgress(_player));
			packet.writeD(reward.getRequiredCompletions());
		}
		return true;
	}
}
