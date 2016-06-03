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

import org.l2junity.gameserver.data.sql.impl.CrestTable;
import org.l2junity.gameserver.model.ClanPrivilege;
import org.l2junity.gameserver.model.Crest;
import org.l2junity.gameserver.model.Crest.CrestType;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * Client packet for setting/deleting clan crest.
 */
public final class RequestSetPledgeCrest implements IClientIncomingPacket
{
	private int _length;
	private byte[] _data = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_length = packet.readD();
		if (_length > 256)
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
		
		if ((_length < 0))
		{
			activeChar.sendPacket(SystemMessageId.THE_SIZE_OF_THE_UPLOADED_SYMBOL_DOES_NOT_MEET_THE_STANDARD_REQUIREMENTS);
			return;
		}
		
		if (_length > 256)
		{
			activeChar.sendPacket(SystemMessageId.THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE_PLEASE_ADJUST_TO_16X12_PIXELS);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
			return;
		}
		
		if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_REGISTER_CREST))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (_length == 0)
		{
			if (clan.getCrestId() != 0)
			{
				clan.changeClanCrest(0);
				activeChar.sendPacket(SystemMessageId.THE_CLAN_MARK_HAS_BEEN_DELETED);
			}
		}
		else
		{
			if (clan.getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLAN_S_SKILL_LEVEL_IS_3_OR_ABOVE);
				return;
			}
			
			final Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.PLEDGE);
			if (crest != null)
			{
				clan.changeClanCrest(crest.getId());
				activeChar.sendPacket(SystemMessageId.THE_CREST_WAS_SUCCESSFULLY_REGISTERED);
			}
		}
	}
	
}
