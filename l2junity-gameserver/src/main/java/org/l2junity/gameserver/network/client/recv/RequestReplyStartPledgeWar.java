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

import org.l2junity.gameserver.data.sql.impl.ClanTable;
import org.l2junity.gameserver.model.ClanWar;
import org.l2junity.gameserver.model.ClanWar.ClanWarState;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestReplyStartPledgeWar implements IClientIncomingPacket
{
	private int _answer;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		packet.readS();
		_answer = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final PlayerInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 1)
		{
			final L2Clan attacked = activeChar.getClan();
			final L2Clan attacker = requestor.getClan();
			if ((attacked != null) && (attacker != null))
			{
				final ClanWar clanWar = attacker.getWarWith(attacked.getId());
				if (clanWar.getState() == ClanWarState.BLOOD_DECLARATION)
				{
					clanWar.mutualClanWarAccepted(attacker, attacked);
					ClanTable.getInstance().storeclanswars(clanWar);
				}
			}
		}
		else
		{
			requestor.sendPacket(SystemMessageId.THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED2);
		}
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
