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
package org.l2junity.gameserver.model.conditions;

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Player Can Resurrect condition implementation.
 * @author UnAfraid
 */
public class ConditionPlayerCanResurrect extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanResurrect(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		// Need skill rework for fix that properly
		if (skill.getAffectRange() > 0)
		{
			return true;
		}
		boolean canResurrect = true;
		
		if (effected.isPlayer())
		{
			final PlayerInstance player = effected.getActingPlayer();
			if (!player.isDead())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
					msg.addSkillName(skill);
					effector.sendPacket(msg);
				}
			}
			else if (player.isResurrectionBlocked())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					effector.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if (player.isReviveRequested())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					effector.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
			}
		}
		else if (effected.isSummon())
		{
			final Summon summon = (Summon) effected;
			final PlayerInstance player = summon.getOwner();
			if (!summon.isDead())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
					msg.addSkillName(skill);
					effector.sendPacket(msg);
				}
			}
			else if (summon.isResurrectionBlocked())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					effector.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if ((player != null) && player.isRevivingPet())
			{
				canResurrect = false;
				if (effector.isPlayer())
				{
					effector.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
				}
			}
		}
		return (_val == canResurrect);
	}
}
