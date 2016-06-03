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
package org.l2junity.gameserver.network.client.recv.mentoring;

import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.mentoring.ExMentorAdd;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author Gnacik, UnAfraid
 */
public class RequestMenteeAdd implements IClientIncomingPacket
{
	private String _target;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_target = packet.readS();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance mentor = client.getActiveChar();
		if (mentor == null)
		{
			return;
		}
		
		final PlayerInstance mentee = World.getInstance().getPlayer(_target);
		if (mentee == null)
		{
			return;
		}
		
		if (ConfirmMenteeAdd.validate(mentor, mentee))
		{
			mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OFFERED_TO_BECOME_S1_S_MENTOR).addCharName(mentee));
			mentee.sendPacket(new ExMentorAdd(mentor));
		}
	}
}