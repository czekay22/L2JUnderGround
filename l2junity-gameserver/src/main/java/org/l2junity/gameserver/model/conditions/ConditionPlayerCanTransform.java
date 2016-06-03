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
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Player Can Transform condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanTransform extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanTransform(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, L2Item item)
	{
		boolean canTransform = true;
		final PlayerInstance player = effector.getActingPlayer();
		if ((player == null) || player.isAlikeDead() || player.isCursedWeaponEquipped())
		{
			canTransform = false;
		}
		else if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
			canTransform = false;
		}
		else if (player.isTransformed())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			canTransform = false;
		}
		else if (player.isInWater())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			canTransform = false;
		}
		else if (player.isFlyingMounted() || player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFORM_WHILE_RIDING_A_PET);
			canTransform = false;
		}
		return (_val == canTransform);
	}
}
