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
import org.l2junity.gameserver.model.TradeList;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * This packet manages the trade response.
 */
public final class TradeDone implements IClientIncomingPacket
{
	private int _response;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_response = packet.readD();
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
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("trade"))
		{
			player.sendMessage("You are trading too fast.");
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			if (Config.DEBUG)
			{
				_log.warn("player.getTradeList == null in " + getClass().getSimpleName() + " for player " + player.getName());
			}
			return;
		}
		
		if (trade.isLocked())
		{
			return;
		}
		
		if (_response == 1)
		{
			if ((trade.getPartner() == null) || (World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null))
			{
				// Trade partner not found, cancel trade
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
				return;
			}
			
			if ((trade.getOwner().hasItemRequest()) || (trade.getPartner().hasItemRequest()))
			{
				return;
			}
			
			if (!player.getAccessLevel().allowTransaction())
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (player.getInstanceWorld() != trade.getPartner().getInstanceWorld())
			{
				player.cancelActiveTrade();
				return;
			}
			
			if (player.calculateDistance(trade.getPartner(), true, false) > 150)
			{
				player.cancelActiveTrade();
				return;
			}
			trade.confirm();
		}
		else
		{
			player.cancelActiveTrade();
		}
	}
}
