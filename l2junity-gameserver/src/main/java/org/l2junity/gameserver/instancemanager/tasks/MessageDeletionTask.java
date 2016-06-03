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
package org.l2junity.gameserver.instancemanager.tasks;

import org.l2junity.gameserver.instancemanager.MailManager;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Message;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message deletion task.
 * @author xban1x
 */
public final class MessageDeletionTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(MessageDeletionTask.class);
	
	final int _msgId;
	
	public MessageDeletionTask(int msgId)
	{
		_msgId = msgId;
	}
	
	@Override
	public void run()
	{
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (msg.hasAttachments())
		{
			try
			{
				final PlayerInstance sender = World.getInstance().getPlayer(msg.getSenderId());
				if (sender != null)
				{
					msg.getAttachments().returnToWh(sender.getWarehouse());
					sender.sendPacket(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME);
				}
				else
				{
					msg.getAttachments().returnToWh(null);
				}
				
				msg.getAttachments().deleteMe();
				msg.removeAttachments();
				
				final PlayerInstance receiver = World.getInstance().getPlayer(msg.getReceiverId());
				if (receiver != null)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME);
					// sm.addString(msg.getReceiverName());
					receiver.sendPacket(sm);
				}
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Error returning items:" + e.getMessage(), e);
			}
		}
		MailManager.getInstance().deleteMessageInDb(msg.getId());
	}
}
