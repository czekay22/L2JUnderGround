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

import java.util.Objects;

import org.l2junity.gameserver.data.sql.impl.ClanTable;
import org.l2junity.gameserver.model.ClanMember;
import org.l2junity.gameserver.model.ClanPrivilege;
import org.l2junity.gameserver.model.ClanWar;
import org.l2junity.gameserver.model.ClanWar.ClanWarState;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

public final class RequestSurrenderPledgeWar implements IClientIncomingPacket
{
	private String _pledgeName;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_pledgeName = packet.readS();
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
		
		final L2Clan myClan = activeChar.getClan();
		if (myClan == null)
		{
			return;
		}
		
		if (myClan.getMembers().stream().filter(Objects::nonNull).filter(ClanMember::isOnline).map(ClanMember::getPlayerInstance).anyMatch(p -> !p.isInCombat()))
		{
			activeChar.sendPacket(SystemMessageId.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Clan targetClan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (targetClan == null)
		{
			activeChar.sendMessage("No such clan.");
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_PLEDGE_WAR))
		{
			client.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ClanWar clanWar = myClan.getWarWith(targetClan.getId());
		
		if (clanWar == null)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1);
			sm.addString(targetClan.getName());
			activeChar.sendPacket(sm);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (clanWar.getState() == ClanWarState.BLOOD_DECLARATION)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DECLARE_DEFEAT_AS_IT_HAS_NOT_BEEN_7_DAYS_SINCE_STARTING_A_CLAN_WAR_WITH_CLAN_S1);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		clanWar.cancel(activeChar, myClan);
	}
}