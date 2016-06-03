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
package org.l2junity.gameserver.network.client.send.mentoring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.instancemanager.MentorManager;
import org.l2junity.gameserver.model.Mentee;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public class ExMentorList implements IClientOutgoingPacket
{
	private final int _type;
	private final Collection<Mentee> _mentees;
	
	public ExMentorList(PlayerInstance activeChar)
	{
		if (activeChar.isMentor())
		{
			_type = 0x01;
			_mentees = MentorManager.getInstance().getMentees(activeChar.getObjectId());
		}
		else if (activeChar.isMentee())
		{
			_type = 0x02;
			_mentees = Arrays.asList(MentorManager.getInstance().getMentor(activeChar.getObjectId()));
		}
		else if (activeChar.isInCategory(CategoryType.AWAKEN_GROUP)) // Not a mentor, Not a mentee, so can be a mentor
		{
			_mentees = Collections.emptyList();
			_type = 0x01;
		}
		else
		{
			_mentees = Collections.emptyList();
			_type = 0x00;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MENTOR_LIST.writeId(packet);
		
		packet.writeD(_type);
		packet.writeD(0x00);
		packet.writeD(_mentees.size());
		for (Mentee mentee : _mentees)
		{
			packet.writeD(mentee.getObjectId());
			packet.writeS(mentee.getName());
			packet.writeD(mentee.getClassId());
			packet.writeD(mentee.getLevel());
			packet.writeD(mentee.isOnlineInt());
		}
		return true;
	}
}
