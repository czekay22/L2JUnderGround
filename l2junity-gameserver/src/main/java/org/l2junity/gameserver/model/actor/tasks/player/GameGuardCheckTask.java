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

import org.l2junity.gameserver.data.xml.impl.AdminData;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.LeaveWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task dedicated to verify client's game guard.
 * @author UnAfraid
 */
public class GameGuardCheckTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(GameGuardCheckTask.class);
	
	private final PlayerInstance _player;
	
	public GameGuardCheckTask(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if ((_player != null))
		{
			L2GameClient client = _player.getClient();
			if ((client != null) && !client.isAuthedGG() && _player.isOnline())
			{
				AdminData.getInstance().broadcastMessageToGMs("Client " + client + " failed to reply GameGuard query and is being kicked!");
				_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
				
				client.close(LeaveWorld.STATIC_PACKET);
			}
		}
	}
}
