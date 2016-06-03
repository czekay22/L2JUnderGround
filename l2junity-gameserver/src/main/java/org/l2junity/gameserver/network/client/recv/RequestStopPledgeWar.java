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
import org.l2junity.gameserver.model.ClanMember;
import org.l2junity.gameserver.model.ClanPrivilege;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2junity.network.PacketReader;

public final class RequestStopPledgeWar implements IClientIncomingPacket
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
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		L2Clan playerClan = player.getClan();
		if (playerClan == null)
		{
			return;
		}
		
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		
		if (clan == null)
		{
			player.sendMessage("No such clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playerClan.isAtWarWith(clan.getId()))
		{
			player.sendMessage("You aren't at war with this clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if player who does the request has the correct rights to do it
		if (!player.hasClanPrivilege(ClanPrivilege.CL_PLEDGE_WAR))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// LOGGER.info("RequestStopPledgeWar: By leader or authorized player: " + playerClan.getLeaderName() + " of clan: "
		// + playerClan.getName() + " to clan: " + _pledgeName);
		
		// L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		// if(leader != null && leader.isOnline() == 0)
		// {
		// player.sendMessage("Clan leader isn't online.");
		// player.sendPacket(ActionFailed.STATIC_PACKET);
		// return;
		// }
		
		// if (leader.isProcessingRequest())
		// {
		// SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		// sm.addString(leader.getName());
		// player.sendPacket(sm);
		// return;
		// }
		
		for (ClanMember member : playerClan.getMembers())
		{
			if ((member == null) || (member.getPlayerInstance() == null))
			{
				continue;
			}
			if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayerInstance()))
			{
				player.sendPacket(SystemMessageId.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE);
				return;
			}
		}
		
		ClanTable.getInstance().deleteclanswars(playerClan.getId(), clan.getId());
		
		for (PlayerInstance member : playerClan.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
		
		for (PlayerInstance member : clan.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
}
