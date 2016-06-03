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

import org.l2junity.gameserver.instancemanager.HandysBlockCheckerManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * Format: chdd d: Arena d: Team
 * @author mrTJO
 */
public final class RequestExCubeGameChangeTeam implements IClientIncomingPacket
{
	private int _arena;
	private int _team;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		// client sends -1,0,1,2 for arena parameter
		_arena = packet.readD() + 1;
		_team = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		// do not remove players after start
		if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(_arena))
		{
			return;
		}
		final PlayerInstance player = client.getActiveChar();
		
		switch (_team)
		{
			case 0:
			case 1:
				// Change Player Team
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(player, _arena, _team);
				break;
			case -1:
			// Remove Player (me)
			{
				int team = HandysBlockCheckerManager.getInstance().getHolder(_arena).getPlayerTeam(player);
				// client sends two times this packet if click on exit
				// client did not send this packet on restart
				if (team > -1)
				{
					HandysBlockCheckerManager.getInstance().removePlayer(player, _arena, team);
				}
				break;
			}
			default:
				_log.warn("Wrong Cube Game Team ID: " + _team);
				break;
		}
	}
}
