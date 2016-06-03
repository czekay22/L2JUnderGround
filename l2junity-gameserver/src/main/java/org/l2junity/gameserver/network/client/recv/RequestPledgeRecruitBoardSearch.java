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

import org.l2junity.gameserver.instancemanager.ClanEntryManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExPledgeRecruitBoardSearch;
import org.l2junity.network.PacketReader;

/**
 * @author Sdw
 */
public class RequestPledgeRecruitBoardSearch implements IClientIncomingPacket
{
	private int _clanLevel;
	private int _karma;
	private int _type;
	private String _query;
	private int _sort;
	private boolean _descending;
	private int _page;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_clanLevel = packet.readD();
		_karma = packet.readD();
		_type = packet.readD();
		_query = packet.readS();
		_sort = packet.readD();
		_descending = packet.readD() == 2;
		_page = packet.readD();
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
		
		if (_query.isEmpty())
		{
			if ((_karma < 0) && (_clanLevel < 0))
			{
				activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getUnSortedClanList(), _page));
			}
			else
			{
				activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanList(_clanLevel, _karma, _sort, _descending), _page));
			}
		}
		else
		{
			activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanListByName(_query.toLowerCase(), _type), _page));
		}
	}
	
}
