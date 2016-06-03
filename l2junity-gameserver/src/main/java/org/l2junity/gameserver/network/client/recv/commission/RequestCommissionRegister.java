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
package org.l2junity.gameserver.network.client.recv.commission;

import org.l2junity.gameserver.instancemanager.CommissionManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.commission.ExCloseCommission;
import org.l2junity.network.PacketReader;

/**
 * @author NosBit
 */
public class RequestCommissionRegister implements IClientIncomingPacket
{
	private int _itemObjectId;
	private long _pricePerUnit;
	private long _itemCount;
	private int _durationType; // -1 = None, 0 = 1 Day, 1 = 3 Days, 2 = 5 Days, 3 = 7 Days
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
		packet.readS(); // Item Name they use it for search we will use server side available names.
		_pricePerUnit = packet.readQ();
		_itemCount = packet.readQ();
		_durationType = packet.readD();
		// packet.readD(); // Unknown
		// packet.readD(); // Unknown
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
		
		if ((_durationType < 0) || (_durationType > 3))
		{
			_log.warn("Player " + player + " sent incorrect commission duration type: " + _durationType + ".");
			return;
		}
		
		if (!CommissionManager.isPlayerAllowedToInteract(player))
		{
			client.sendPacket(ExCloseCommission.STATIC_PACKET);
			return;
		}
		
		CommissionManager.getInstance().registerItem(player, _itemObjectId, _itemCount, _pricePerUnit, (byte) ((_durationType * 2) + 1));
	}
}
