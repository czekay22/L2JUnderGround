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
package org.l2junity.gameserver.model.actor.instance;

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ai.CharacterAI;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.ai.FriendlyNpcAI;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.character.npc.OnNpcFirstTalk;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.SocialAction;

/**
 * @author Sdw
 */
public class FriendlyNpcInstance extends Attackable
{
	public FriendlyNpcInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.FriendlyNpcInstance);
	}
	
	@Override
	public boolean isAttackable()
	{
		return false;
	}
	
	@Override
	public void onAction(PlayerInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2GuardInstance
		if (getObjectId() != player.getTargetId())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Set the L2PcInstance Intention to AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
				// to display a social action of the L2GuardInstance on their client
				broadcastPacket(new SocialAction(getObjectId(), Rnd.nextInt(8)));
				
				player.setLastFolkNPC(this);
				
				// Open a chat window on client with the text of the L2GuardInstance
				if (hasListener(EventType.ON_NPC_QUEST_START))
				{
					player.setLastQuestNpcObject(getObjectId());
				}
				
				if (hasListener(EventType.ON_NPC_FIRST_TALK))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcFirstTalk(this, player), this);
				}
				else
				{
					showChatWindow(player, 0);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/default/" + pom + ".htm";
	}
	
	@Override
	protected CharacterAI initAI()
	{
		return new FriendlyNpcAI(this);
	}
}
