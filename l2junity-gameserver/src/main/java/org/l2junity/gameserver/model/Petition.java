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
package org.l2junity.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2junity.gameserver.enums.PetitionState;
import org.l2junity.gameserver.enums.PetitionType;
import org.l2junity.gameserver.idfactory.IdFactory;
import org.l2junity.gameserver.instancemanager.PetitionManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.PetitionVotePacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Petition
 * @author xban1x
 */
public final class Petition
{
	private final long _submitTime = System.currentTimeMillis();
	private final int _id;
	private final PetitionType _type;
	private PetitionState _state = PetitionState.PENDING;
	private final String _content;
	private final List<CreatureSay> _messageLog = new CopyOnWriteArrayList<>();
	private final PlayerInstance _petitioner;
	private PlayerInstance _responder;
	
	public Petition(PlayerInstance petitioner, String petitionText, int petitionType)
	{
		_id = IdFactory.getInstance().getNextId();
		_type = PetitionType.values()[--petitionType];
		_content = petitionText;
		_petitioner = petitioner;
	}
	
	public boolean addLogMessage(CreatureSay cs)
	{
		return _messageLog.add(cs);
	}
	
	public List<CreatureSay> getLogMessages()
	{
		return _messageLog;
	}
	
	public boolean endPetitionConsultation(PetitionState endState)
	{
		setState(endState);
		
		if ((getResponder() != null) && getResponder().isOnline())
		{
			if (endState == PetitionState.RESPONDER_REJECT)
			{
				getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
			}
			else
			{
				// Ending petition consultation with <Player>.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_CONSULTATION_WITH_C1_HAS_ENDED);
				sm.addString(getPetitioner().getName());
				getResponder().sendPacket(sm);
				
				if (endState == PetitionState.PETITIONER_CANCEL)
				{
					// Receipt No. <ID> petition cancelled.
					sm = SystemMessage.getSystemMessage(SystemMessageId.RECEIPT_NO_S1_PETITION_CANCELLED);
					sm.addInt(getId());
					getResponder().sendPacket(sm);
				}
			}
		}
		
		// End petition consultation and inform them, if they are still online. And if petitioner is online, enable Evaluation button
		if ((getPetitioner() != null) && getPetitioner().isOnline())
		{
			getPetitioner().sendPacket(SystemMessageId.THIS_ENDS_THE_GM_PETITION_CONSULTATION_NPLEASE_GIVE_US_FEEDBACK_ON_THE_PETITION_SERVICE);
			getPetitioner().sendPacket(PetitionVotePacket.STATIC_PACKET);
		}
		
		PetitionManager.getInstance().getCompletedPetitions().put(getId(), this);
		return (PetitionManager.getInstance().getPendingPetitions().remove(getId()) != null);
	}
	
	public String getContent()
	{
		return _content;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public PlayerInstance getPetitioner()
	{
		return _petitioner;
	}
	
	public PlayerInstance getResponder()
	{
		return _responder;
	}
	
	public long getSubmitTime()
	{
		return _submitTime;
	}
	
	public PetitionState getState()
	{
		return _state;
	}
	
	public String getTypeAsString()
	{
		return _type.toString().replace("_", " ");
	}
	
	public void sendPetitionerPacket(IClientOutgoingPacket responsePacket)
	{
		if ((getPetitioner() == null) || !getPetitioner().isOnline())
		{
			// Allows petitioners to see the results of their petition when
			// they log back into the game.
			
			// endPetitionConsultation(PetitionState.Petitioner_Missing);
			return;
		}
		
		getPetitioner().sendPacket(responsePacket);
	}
	
	public void sendResponderPacket(IClientOutgoingPacket responsePacket)
	{
		if ((getResponder() == null) || !getResponder().isOnline())
		{
			endPetitionConsultation(PetitionState.RESPONDER_MISSING);
			return;
		}
		
		getResponder().sendPacket(responsePacket);
	}
	
	public void setState(PetitionState state)
	{
		_state = state;
	}
	
	public void setResponder(PlayerInstance respondingAdmin)
	{
		if (getResponder() != null)
		{
			return;
		}
		
		_responder = respondingAdmin;
	}
}
