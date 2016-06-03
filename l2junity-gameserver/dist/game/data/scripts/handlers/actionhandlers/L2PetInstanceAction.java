/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.actionhandlers;

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.handler.IActionHandler;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerSummonTalk;
import org.l2junity.gameserver.network.client.send.PetStatusShow;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

public class L2PetInstanceAction implements IActionHandler
{
	@Override
	public boolean action(PlayerInstance activeChar, WorldObject target, boolean interact)
	{
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ENMITY);
			return false;
		}
		
		boolean isOwner = activeChar.getObjectId() == ((L2PetInstance) target).getOwner().getObjectId();
		
		if (isOwner && (activeChar != ((L2PetInstance) target).getOwner()))
		{
			((L2PetInstance) target).updateRefOwner(activeChar);
		}
		if (activeChar.getTarget() != target)
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			// Check if the pet is attackable (without a forced attack) and isn't dead
			if (target.isAutoAttackable(activeChar) && !isOwner)
			{
				if (GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					activeChar.onActionRequest();
				}
			}
			else if (!((Creature) target).isInsideRadius(activeChar, 150, false, false))
			{
				if (GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
					activeChar.onActionRequest();
				}
			}
			else
			{
				if (isOwner)
				{
					activeChar.sendPacket(new PetStatusShow((L2PetInstance) target));
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSummonTalk((Summon) target), (Summon) target);
				}
				activeChar.updateNotMoveUntil();
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2PetInstance;
	}
}
