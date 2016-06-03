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

import java.util.concurrent.TimeUnit;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Task dedicated to warn user to take a break.
 * @author UnAfraid
 */
public final class WarnUserTakeBreakTask implements Runnable
{
	private final PlayerInstance _player;
	
	public WarnUserTakeBreakTask(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player != null)
		{
			if (_player.isOnline())
			{
				final long hours = TimeUnit.MILLISECONDS.toHours(_player.getUptime());
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_PLAYED_FOR_S1_HOUR_S_PLEASE_TAKE_A_BREAK).addLong(hours));
			}
			else
			{
				_player.stopWarnUserTakeBreak();
			}
		}
	}
}
