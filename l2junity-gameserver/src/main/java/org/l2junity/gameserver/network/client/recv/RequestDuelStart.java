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
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExDuelAskStart;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * Format:(ch) Sd
 * @author -Wooden-
 */
public final class RequestDuelStart implements IClientIncomingPacket
{
	private String _player;
	private int _partyDuel;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_player = packet.readS();
		_partyDuel = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance activeChar = client.getActiveChar();
		PlayerInstance targetChar = World.getInstance().getPlayer(_player);
		if (activeChar == null)
		{
			return;
		}
		if (targetChar == null)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}
		if (activeChar == targetChar)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}
		
		// Check if duel is possible
		if (!activeChar.canDuel())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return;
		}
		else if (!targetChar.canDuel())
		{
			activeChar.sendPacket(targetChar.getNoDuelReason());
			return;
		}
		// Players may not be too far apart
		else if (!activeChar.isInsideRadius(targetChar, 250, false, false))
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_TOO_FAR_AWAY_TO_RECEIVE_A_DUEL_CHALLENGE);
			msg.addString(targetChar.getName());
			activeChar.sendPacket(msg);
			return;
		}
		
		// Duel is a party duel
		if (_partyDuel == 1)
		{
			// Player must be in a party & the party leader
			final Party party = activeChar.getParty();
			if ((party == null) || !party.isLeader(activeChar))
			{
				activeChar.sendMessage("You have to be the leader of a party in order to request a party duel.");
				return;
			}
			// Target must be in a party
			else if (!targetChar.isInParty())
			{
				activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
				return;
			}
			// Target may not be of the same party
			else if (activeChar.getParty().containsPlayer(targetChar))
			{
				activeChar.sendMessage("This player is a member of your own party.");
				return;
			}
			
			// Check if every player is ready for a duel
			for (PlayerInstance temp : activeChar.getParty().getMembers())
			{
				if (!temp.canDuel())
				{
					activeChar.sendMessage("Not all the members of your party are ready for a duel.");
					return;
				}
			}
			PlayerInstance partyLeader = null; // snatch party leader of targetChar's party
			for (PlayerInstance temp : targetChar.getParty().getMembers())
			{
				if (partyLeader == null)
				{
					partyLeader = temp;
				}
				if (!temp.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
					return;
				}
			}
			
			// Send request to targetChar's party leader
			if (partyLeader != null)
			{
				if (!partyLeader.isProcessingRequest())
				{
					activeChar.onTransactionRequest(partyLeader);
					partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
					
					if (Config.DEBUG)
					{
						_log.debug(activeChar.getName() + " requested a duel with " + partyLeader.getName());
					}
					
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
					
					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
					msg.addString(activeChar.getName());
					targetChar.sendPacket(msg);
				}
				else
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
				}
			}
		}
		else
		// 1vs1 duel
		{
			if (!targetChar.isProcessingRequest())
			{
				activeChar.onTransactionRequest(targetChar);
				targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
				
				if (Config.DEBUG)
				{
					_log.debug(activeChar.getName() + " requested a duel with " + targetChar.getName());
				}
				
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_CHALLENGED_YOU_TO_A_DUEL);
				msg.addString(activeChar.getName());
				targetChar.sendPacket(msg);
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
			}
		}
	}
}
