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

import org.l2junity.gameserver.instancemanager.FortManager;
import org.l2junity.gameserver.model.entity.Fort;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExShowFortressMapInfo;
import org.l2junity.network.PacketReader;

/**
 * @author KenM
 */
public class RequestFortressMapInfo implements IClientIncomingPacket
{
	private int _fortressId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_fortressId = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final Fort fort = FortManager.getInstance().getFortById(_fortressId);
		if (fort == null)
		{
			_log.warn("Fort is not found with id (" + _fortressId + ") in all forts with size of (" + FortManager.getInstance().getForts().size() + ") called by player (" + client.getActiveChar() + ")");
			
			if (client.getActiveChar() == null)
			{
				return;
			}
			
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		client.sendPacket(new ExShowFortressMapInfo(fort));
	}
}
