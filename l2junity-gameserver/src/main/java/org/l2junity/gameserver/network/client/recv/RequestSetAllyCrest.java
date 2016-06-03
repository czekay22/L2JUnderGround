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
import org.l2junity.gameserver.data.sql.impl.CrestTable;
import org.l2junity.gameserver.model.Crest;
import org.l2junity.gameserver.model.Crest.CrestType;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * Client packet for setting ally crest.
 */
public final class RequestSetAllyCrest implements IClientIncomingPacket
{
	private int _length;
	private byte[] _data = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_length = packet.readD();
		if (_length > 192)
		{
			return false;
		}
		
		_data = packet.readB(_length);
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
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 192)
		{
			activeChar.sendPacket(SystemMessageId.PLEASE_ADJUST_THE_IMAGE_SIZE_TO_8X12);
			return;
		}
		
		if (activeChar.getAllyId() == 0)
		{
			activeChar.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS);
			return;
		}
		
		final L2Clan leaderClan = ClanTable.getInstance().getClan(activeChar.getAllyId());
		
		if ((activeChar.getClanId() != leaderClan.getId()) || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_TO_ALLIANCE_LEADERS);
			return;
		}
		
		if (_length == 0)
		{
			if (leaderClan.getAllyCrestId() != 0)
			{
				leaderClan.changeAllyCrest(0, false);
			}
		}
		else
		{
			final Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.ALLY);
			if (crest != null)
			{
				leaderClan.changeAllyCrest(crest.getId(), false);
				activeChar.sendPacket(SystemMessageId.THE_CREST_WAS_SUCCESSFULLY_REGISTERED);
			}
		}
	}
}
