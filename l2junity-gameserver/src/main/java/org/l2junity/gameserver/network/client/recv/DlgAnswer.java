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

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.AdminData;
import org.l2junity.gameserver.enums.PlayerAction;
import org.l2junity.gameserver.handler.AdminCommandHandler;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerDlgAnswer;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.holders.DoorRequestHolder;
import org.l2junity.gameserver.model.holders.SummonRequestHolder;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.GMAudit;
import org.l2junity.network.PacketReader;

/**
 * @author Dezmond_snz
 */
public final class DlgAnswer implements IClientIncomingPacket
{
	private int _messageId;
	private int _answer;
	private int _requesterId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_messageId = packet.readD();
		_answer = packet.readD();
		_requesterId = packet.readD();
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
		
		final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnPlayerDlgAnswer(activeChar, _messageId, _answer, _requesterId), activeChar, TerminateReturn.class);
		if ((term != null) && term.terminate())
		{
			return;
		}
		
		if (_messageId == SystemMessageId.S13.getId())
		{
			if (activeChar.removeAction(PlayerAction.ADMIN_COMMAND))
			{
				String cmd = activeChar.getAdminConfirmCmd();
				activeChar.setAdminConfirmCmd(null);
				if (_answer == 0)
				{
					return;
				}
				String command = cmd.split(" ")[0];
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				if (AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
					}
					ach.useAdminCommand(cmd, activeChar);
				}
			}
		}
		else if ((_messageId == SystemMessageId.C1_IS_ATTEMPTING_TO_DO_A_RESURRECTION_THAT_RESTORES_S2_S3_XP_ACCEPT.getId()) || (_messageId == SystemMessageId.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU_WOULD_YOU_LIKE_TO_RESURRECT_NOW.getId()))
		{
			activeChar.reviveAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			final SummonRequestHolder holder = activeChar.removeScript(SummonRequestHolder.class);
			if ((_answer == 1) && (holder != null) && (holder.getTarget().getObjectId() == _requesterId))
			{
				activeChar.teleToLocation(holder.getTarget().getLocation(), true);
			}
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			final DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
			if ((holder != null) && (holder.getDoor() == activeChar.getTarget()) && (_answer == 1))
			{
				holder.getDoor().openMe();
			}
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			final DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
			if ((holder != null) && (holder.getDoor() == activeChar.getTarget()) && (_answer == 1))
			{
				holder.getDoor().closeMe();
			}
		}
	}
}
