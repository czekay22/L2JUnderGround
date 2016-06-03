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
package org.l2junity.gameserver.model.actor.tasks.player;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Task dedicated to increase player's recommendation bonus.
 * @author UnAfraid
 */
public class RecoGiveTask implements Runnable
{
	private final PlayerInstance _player;
	
	public RecoGiveTask(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player != null)
		{
			// 10 recommendations to give out after 2 hours of being logged in
			// 1 more recommendation to give out every hour after that.
			int recoToGive = 1;
			if (!_player.isRecoTwoHoursGiven())
			{
				recoToGive = 10;
				_player.setRecoTwoHoursGiven(true);
			}
			
			_player.setRecomLeft(_player.getRecomLeft() + recoToGive);
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATION_S);
			sm.addInt(recoToGive);
			_player.sendPacket(sm);
			_player.sendPacket(new UserInfo(_player));
		}
	}
}
