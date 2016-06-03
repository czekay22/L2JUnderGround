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

import org.l2junity.gameserver.enums.UserInfoType;
import org.l2junity.gameserver.instancemanager.ClanEntryManager;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.JoinPledge;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.network.PacketReader;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUserAccept implements IClientIncomingPacket
{
	private boolean _acceptRequest;
	private int _playerId;
	private int _clanId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_acceptRequest = packet.readD() == 1;
		_playerId = packet.readD();
		_clanId = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			return;
		}
		
		if (_acceptRequest)
		{
			final PlayerInstance player = World.getInstance().getPlayer(_playerId);
			if (player != null)
			{
				final L2Clan clan = activeChar.getClan();
				clan.addClanMember(player);
				player.sendPacket(new JoinPledge(_clanId));
				final UserInfo ui = new UserInfo(player);
				ui.addComponentType(UserInfoType.CLAN);
				player.sendPacket(ui);
				player.broadcastInfo();
				
				ClanEntryManager.getInstance().removePlayerApplication(clan.getId(), _playerId);
			}
		}
		else
		{
			ClanEntryManager.getInstance().removePlayerApplication(activeChar.getClanId(), _playerId);
		}
	}
}
