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

import org.l2junity.gameserver.instancemanager.DuelManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * Format:(ch) ddd
 * @author -Wooden-
 */
public final class RequestDuelAnswerStart implements IClientIncomingPacket
{
	private int _partyDuel;
	@SuppressWarnings("unused")
	private int _unk1;
	private int _response;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_partyDuel = packet.readD();
		_unk1 = packet.readD();
		_response = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		PlayerInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_response == 1)
		{
			SystemMessage msg1 = null, msg2 = null;
			if (requestor.isInDuel())
			{
				msg1 = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL);
				msg1.addString(requestor.getName());
				player.sendPacket(msg1);
				return;
			}
			else if (player.isInDuel())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}
			
			if (_partyDuel == 1)
			{
				msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg1.addString(requestor.getName());
				
				msg2 = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg2.addString(player.getName());
			}
			else
			{
				msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_S_CHALLENGE_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg1.addString(requestor.getName());
				
				msg2 = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg2.addString(player.getName());
			}
			
			player.sendPacket(msg1);
			requestor.sendPacket(msg2);
			
			DuelManager.getInstance().addDuel(requestor, player, _partyDuel);
		}
		else if (_response == -1)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_DUEL_REQUESTS_AND_CANNOT_RECEIVE_A_DUEL_REQUEST);
			sm.addPcName(player);
			requestor.sendPacket(sm);
		}
		else
		{
			SystemMessage msg = null;
			if (_partyDuel == 1)
			{
				msg = SystemMessage.getSystemMessage(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
			}
			else
			{
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
				msg.addPcName(player);
			}
			requestor.sendPacket(msg);
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
