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

import org.l2junity.gameserver.model.ClanInfo;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.AllianceInfo;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1479 $ $Date: 2005-11-09 00:47:42 +0100 (mer., 09 nov. 2005) $
 */
public final class RequestAllyInfo implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
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
		
		SystemMessage sm;
		final int allianceId = activeChar.getAllyId();
		if (allianceId > 0)
		{
			final AllianceInfo ai = new AllianceInfo(allianceId);
			client.sendPacket(ai);
			
			// send for player
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFORMATION);
			client.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
			sm.addString(ai.getName());
			client.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
			sm.addString(ai.getLeaderC());
			sm.addString(ai.getLeaderP());
			client.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
			sm.addInt(ai.getOnline());
			sm.addInt(ai.getTotal());
			client.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.AFFILIATED_CLANS_TOTAL_S1_CLAN_S);
			sm.addInt(ai.getAllies().length);
			client.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFORMATION);
			for (final ClanInfo aci : ai.getAllies())
			{
				client.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_S1);
				sm.addString(aci.getClan().getName());
				client.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_S1);
				sm.addString(aci.getClan().getLeaderName());
				client.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_S1);
				sm.addInt(aci.getClan().getLevel());
				client.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
				sm.addInt(aci.getOnline());
				sm.addInt(aci.getTotal());
				client.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.EMPTY4);
			}
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.EMPTY5);
			client.sendPacket(sm);
		}
		else
		{
			client.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
		}
	}
}
