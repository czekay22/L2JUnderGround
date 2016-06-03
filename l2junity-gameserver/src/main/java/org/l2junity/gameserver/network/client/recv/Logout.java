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
package org.l2junity.gameserver.network.client.recv;

import org.l2junity.Config;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.L2Event;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class Logout implements IClientIncomingPacket
{
	protected static final Logger _logAccounting = LoggerFactory.getLogger("accounting");
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
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
		
		if (player.hasItemRequest())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isLocked())
		{
			_log.warn("Player " + player.getName() + " tried to logout during class change.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't allow leaving if player is fighting
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			if (player.isGM() && Config.GM_RESTART_FIGHTING)
			{
				return;
			}
			
			player.sendPacket(SystemMessageId.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (L2Event.isParticipant(player))
		{
			player.sendMessage("A superior power doesn't allow you to leave the event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		_logAccounting.info("Disconnected, {}", client);
		
		player.logout();
	}
}
