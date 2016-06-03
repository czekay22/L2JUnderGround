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
package org.l2junity.gameserver.network.client.recv.sayune;

import org.l2junity.gameserver.data.xml.impl.SayuneData;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.SayuneEntry;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.request.SayuneRequest;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.type.SayuneZone;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestFlyMoveStart implements IClientIncomingPacket
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
		if ((activeChar == null) || !activeChar.isInsideZone(ZoneId.SAYUNE) || activeChar.hasRequest(SayuneRequest.class) || !activeChar.isInCategory(CategoryType.AWAKEN_GROUP))
		{
			return;
		}
		
		if (activeChar.hasSummon())
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_USE_SAYUNE_WHILE_PET_OR_SUMMONED_PET_IS_OUT);
			return;
		}
		
		if (activeChar.getReputation() < 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_SAYUNE_WHILE_IN_A_CHAOTIC_STATE);
			return;
		}
		
		if (activeChar.hasRequests())
		{
			activeChar.sendPacket(SystemMessageId.SAYUNE_CANNOT_BE_USED_WHILE_TAKING_OTHER_ACTIONS);
			return;
		}
		
		final SayuneZone zone = ZoneManager.getInstance().getZone(activeChar, SayuneZone.class);
		if (zone.getMapId() == -1)
		{
			activeChar.sendMessage("That zone is not supported yet!");
			_log.warn(getClass().getSimpleName() + ": " + activeChar + " Requested sayune on zone with no map id set");
			return;
		}
		
		final SayuneEntry map = SayuneData.getInstance().getMap(zone.getMapId());
		if (map == null)
		{
			activeChar.sendMessage("This zone is not handled yet!!");
			_log.warn(getClass().getSimpleName() + ": " + activeChar + " Requested sayune on unhandled map zone " + zone.getName());
			return;
		}
		
		final SayuneRequest request = new SayuneRequest(activeChar, map.getId());
		if (activeChar.addRequest(request))
		{
			request.move(activeChar, 0);
		}
	}
}
