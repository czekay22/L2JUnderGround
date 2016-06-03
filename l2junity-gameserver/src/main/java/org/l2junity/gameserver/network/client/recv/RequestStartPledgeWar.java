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

import org.l2junity.Config;
import org.l2junity.gameserver.data.sql.impl.ClanTable;
import org.l2junity.gameserver.enums.UserInfoType;
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

public final class RequestStartPledgeWar implements IClientIncomingPacket
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
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2Clan clanDeclaringWar = player.getClan();
		if (clanDeclaringWar == null)
		{
			return;
		}
		
		if ((clanDeclaringWar.getLevel() < 5) || (clanDeclaringWar.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR))
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_CLAN_WAR_CAN_ONLY_BE_DECLARED_IF_THE_CLAN_IS_LEVEL_5_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (!player.hasClanPrivilege(ClanPrivilege.CL_PLEDGE_WAR))
		{
			client.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (clanDeclaringWar.getWarCount() >= 30)
		{
			client.sendPacket(SystemMessageId.A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CAN_T_BE_MADE_AT_THE_SAME_TIME);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Clan clanDeclaredWar = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clanDeclaredWar == null)
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (clanDeclaredWar == clanDeclaringWar)
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if ((clanDeclaringWar.getAllyId() == clanDeclaredWar.getAllyId()) && (clanDeclaringWar.getAllyId() != 0))
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CAN_T_BE_MADE));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if ((clanDeclaredWar.getLevel() < 5) || (clanDeclaredWar.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR))
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_CLAN_WAR_CAN_ONLY_BE_DECLARED_IF_THE_CLAN_IS_LEVEL_5_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (clanDeclaredWar.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_CLAN_WAR_CAN_NOT_BE_DECLARED_AGAINST_A_CLAN_THAT_IS_BEING_DISSOLVED));
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ClanWar clanWar = clanDeclaringWar.getWarWith(clanDeclaredWar.getId());
		if (clanWar != null)
		{
			if (clanWar.getClanWarState(clanDeclaringWar) == ClanWarState.WIN)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_T_DECLARE_A_WAR_BECAUSE_THE_21_DAY_PERIOD_HASN_T_PASSED_AFTER_A_DEFEAT_DECLARATION_WITH_THE_S1_CLAN);
				sm.addString(clanDeclaredWar.getName());
				client.sendPacket(sm);
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN);
			sm.addString(clanDeclaredWar.getName());
			client.sendPacket(sm);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ClanWar newClanWar = new ClanWar(clanDeclaringWar, clanDeclaredWar);
		
		ClanTable.getInstance().storeclanswars(newClanWar);
		
		clanDeclaringWar.getMembers().stream().filter(Objects::nonNull).filter(ClanMember::isOnline).forEach(p -> p.getPlayerInstance().broadcastUserInfo(UserInfoType.CLAN));
		
		clanDeclaredWar.getMembers().stream().filter(Objects::nonNull).filter(ClanMember::isOnline).forEach(p -> p.getPlayerInstance().broadcastUserInfo(UserInfoType.CLAN));
	}
}
