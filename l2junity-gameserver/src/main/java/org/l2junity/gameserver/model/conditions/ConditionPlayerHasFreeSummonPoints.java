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
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * @author Sdw
 */
public class ConditionPlayerHasFreeSummonPoints extends Condition
{
	private final int _summonPoints;
	
	public ConditionPlayerHasFreeSummonPoints(int summonPoints)
	{
		_summonPoints = summonPoints;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		final PlayerInstance player = effector.getActingPlayer();
		if (player == null)
		{
			return false;
		}
		
		boolean canSummon = true;
		
		if ((_summonPoints == 0) && player.hasServitors())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_S1_SKILL_DUE_TO_INSUFFICIENT_SUMMON_POINTS);
			canSummon = false;
		}
		else if ((player.getSummonPoints() + _summonPoints) > player.getMaxSummonPoints())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_THE_S1_SKILL_DUE_TO_INSUFFICIENT_SUMMON_POINTS);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			canSummon = false;
		}
		
		return canSummon;
	}
}
