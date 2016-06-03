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
package org.l2junity.gameserver.network.client.recv.onedayreward;

import java.util.Collection;

import org.l2junity.gameserver.data.xml.impl.OneDayRewardData;
import org.l2junity.gameserver.model.OneDayRewardDataHolder;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.onedayreward.ExOneDayReceiveRewardList;
import org.l2junity.network.PacketReader;

/**
 * @author Sdw
 */
public class RequestOneDayRewardReceive implements IClientIncomingPacket
{
	private int _id;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_id = packet.readH();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final Collection<OneDayRewardDataHolder> reward = OneDayRewardData.getInstance().getOneDayRewardData(_id);
		if (reward.isEmpty())
		{
			return;
		}
		
		reward.stream().filter(o -> o.isDisplayable(player)).forEach(r -> r.requestReward(player));
		player.sendPacket(new ExOneDayReceiveRewardList(player));
	}
}
