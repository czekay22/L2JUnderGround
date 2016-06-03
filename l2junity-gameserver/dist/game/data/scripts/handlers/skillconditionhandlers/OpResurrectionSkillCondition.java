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
package handlers.skillconditionhandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * @author Sdw
 */
public class OpResurrectionSkillCondition implements ISkillCondition
{
	public OpResurrectionSkillCondition(StatsSet params)
	{
		
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		boolean canResurrect = true;
		
		if (target == caster)
		{
			return canResurrect;
		}
		
		if (target.isPlayer())
		{
			final PlayerInstance player = target.getActingPlayer();
			if (!player.isDead())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
					msg.addSkillName(skill);
					caster.sendPacket(msg);
				}
			}
			else if (player.isResurrectionBlocked())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if (player.isReviveRequested())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
			}
		}
		else if (target.isSummon())
		{
			final Summon summon = (Summon) target;
			final PlayerInstance player = target.getActingPlayer();
			if (!summon.isDead())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
					msg.addSkillName(skill);
					caster.sendPacket(msg);
				}
			}
			else if (summon.isResurrectionBlocked())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				}
			}
			else if ((player != null) && player.isRevivingPet())
			{
				canResurrect = false;
				if (caster.isPlayer())
				{
					caster.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED);
				}
			}
		}
		
		return canResurrect;
	}
}
