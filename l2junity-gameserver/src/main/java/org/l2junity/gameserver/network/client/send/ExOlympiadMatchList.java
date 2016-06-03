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

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.olympiad.AbstractOlympiadGame;
import org.l2junity.gameserver.model.olympiad.OlympiadGameClassed;
import org.l2junity.gameserver.model.olympiad.OlympiadGameManager;
import org.l2junity.gameserver.model.olympiad.OlympiadGameNonClassed;
import org.l2junity.gameserver.model.olympiad.OlympiadGameTask;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author mrTJO
 */
public class ExOlympiadMatchList implements IClientOutgoingPacket
{
	private final List<OlympiadGameTask> _games = new ArrayList<>();
	
	public ExOlympiadMatchList()
	{
		OlympiadGameTask task;
		for (int i = 0; i < OlympiadGameManager.getInstance().getNumberOfStadiums(); i++)
		{
			task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task != null)
			{
				if (!task.isGameStarted() || task.isBattleFinished())
				{
					continue; // initial or finished state not shown
				}
				_games.add(task);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RECEIVE_OLYMPIAD.writeId(packet);
		
		packet.writeD(0x00); // Type 0 = Match List, 1 = Match Result
		
		packet.writeD(_games.size());
		packet.writeD(0x00);
		
		for (OlympiadGameTask curGame : _games)
		{
			AbstractOlympiadGame game = curGame.getGame();
			if (game != null)
			{
				packet.writeD(game.getStadiumId()); // Stadium Id (Arena 1 = 0)
				
				if (game instanceof OlympiadGameNonClassed)
				{
					packet.writeD(1);
				}
				else if (game instanceof OlympiadGameClassed)
				{
					packet.writeD(2);
				}
				else
				{
					packet.writeD(0);
				}
				
				packet.writeD(curGame.isRunning() ? 0x02 : 0x01); // (1 = Standby, 2 = Playing)
				packet.writeS(game.getPlayerNames()[0]); // Player 1 Name
				packet.writeS(game.getPlayerNames()[1]); // Player 2 Name
			}
		}
		return true;
	}
}
