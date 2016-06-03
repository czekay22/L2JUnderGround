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

import org.l2junity.Config;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Task dedicated to reward player with fame while standing on siege zone.
 * @author UnAfraid
 */
public class FameTask implements Runnable
{
	private final PlayerInstance _player;
	private final int _value;
	
	public FameTask(PlayerInstance player, int value)
	{
		_player = player;
		_value = value;
	}
	
	@Override
	public void run()
	{
		if ((_player == null) || (_player.isDead() && !Config.FAME_FOR_DEAD_PLAYERS))
		{
			return;
		}
		if (((_player.getClient() == null) || _player.getClient().isDetached()) && !Config.OFFLINE_FAME)
		{
			return;
		}
		_player.setFame(_player.getFame() + _value);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_FAME);
		sm.addInt(_value);
		_player.sendPacket(sm);
		_player.sendPacket(new UserInfo(_player));
	}
}
