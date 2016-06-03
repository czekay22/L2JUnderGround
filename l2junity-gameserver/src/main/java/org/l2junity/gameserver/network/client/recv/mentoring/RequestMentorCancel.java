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

import org.l2junity.Config;
import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.instancemanager.MentorManager;
import org.l2junity.gameserver.model.Mentee;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerMenteeLeft;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerMenteeRemove;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestMentorCancel implements IClientIncomingPacket
{
	private int _confirmed;
	private String _name;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_confirmed = packet.readD();
		_name = packet.readS();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if (_confirmed != 1)
		{
			return;
		}
		
		PlayerInstance player = client.getActiveChar();
		int objectId = CharNameTable.getInstance().getIdByName(_name);
		if (player != null)
		{
			if (player.isMentor())
			{
				final Mentee mentee = MentorManager.getInstance().getMentee(player.getObjectId(), objectId);
				if (mentee != null)
				{
					MentorManager.getInstance().cancelAllMentoringBuffs(mentee.getPlayerInstance());
					
					if (MentorManager.getInstance().isAllMenteesOffline(player.getObjectId(), mentee.getObjectId()))
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(player);
					}
					
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED_THE_MENTOR_CANNOT_OBTAIN_ANOTHER_MENTEE_FOR_TWO_DAYS).addString(_name));
					MentorManager.getInstance().setPenalty(player.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(player.getObjectId(), mentee.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeRemove(player, mentee), player);
				}
				
			}
			else if (player.isMentee())
			{
				final Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
				if ((mentor != null) && (mentor.getObjectId() == objectId))
				{
					MentorManager.getInstance().cancelAllMentoringBuffs(player);
					
					if (MentorManager.getInstance().isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
					{
						MentorManager.getInstance().cancelAllMentoringBuffs(mentor.getPlayerInstance());
					}
					
					MentorManager.getInstance().setPenalty(mentor.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeLeft(mentor, player), player);
					
					mentor.getPlayerInstance().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED_THE_MENTOR_CANNOT_OBTAIN_ANOTHER_MENTEE_FOR_TWO_DAYS).addString(_name));
				}
			}
		}
	}
}
