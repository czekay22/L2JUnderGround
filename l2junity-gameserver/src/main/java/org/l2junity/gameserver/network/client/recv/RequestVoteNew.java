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

import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExVoteSystemInfo;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

public final class RequestVoteNew implements IClientIncomingPacket
{
	private int _targetId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_targetId = packet.readD();
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
		
		WorldObject object = activeChar.getTarget();
		
		if (!(object instanceof PlayerInstance))
		{
			if (object == null)
			{
				client.sendPacket(SystemMessageId.SELECT_TARGET);
			}
			else
			{
				client.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			}
			return;
		}
		
		PlayerInstance target = (PlayerInstance) object;
		
		if (target.getObjectId() != _targetId)
		{
			return;
		}
		
		if (target == activeChar)
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		
		if (activeChar.getRecomLeft() <= 0)
		{
			client.sendPacket(SystemMessageId.YOU_ARE_OUT_OF_RECOMMENDATIONS_TRY_AGAIN_LATER);
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			client.sendPacket(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		
		activeChar.giveRecom(target);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addInt(activeChar.getRecomLeft());
		client.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(activeChar);
		target.sendPacket(sm);
		
		client.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo();
		
		client.sendPacket(new ExVoteSystemInfo(activeChar));
		target.sendPacket(new ExVoteSystemInfo(target));
	}
}
