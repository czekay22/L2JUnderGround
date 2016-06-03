/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.network.client.recv.ceremonyofchaos;

import org.l2junity.gameserver.enums.CeremonyOfChaosState;
import org.l2junity.gameserver.instancemanager.CeremonyOfChaosManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author Sdw
 */
public class RequestCuriousHouseHtml implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		// Nothing to read
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
		
		if (CeremonyOfChaosManager.getInstance().getState() != CeremonyOfChaosState.REGISTRATION)
		{
			return;
		}
		else if (CeremonyOfChaosManager.getInstance().isRegistered(player))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_ON_THE_WAITING_LIST_FOR_THE_CEREMONY_OF_CHAOS);
			return;
		}
		
		if (CeremonyOfChaosManager.getInstance().canRegister(player, true))
		{
			final NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile(player.getHtmlPrefix(), "data/html/CeremonyOfChaos/invite.htm");
			player.sendPacket(message);
		}
	}
}
