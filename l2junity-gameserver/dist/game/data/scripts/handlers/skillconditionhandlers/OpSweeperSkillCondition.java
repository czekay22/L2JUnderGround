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

import java.util.concurrent.atomic.AtomicBoolean;

import org.l2junity.Config;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * @author Sdw
 */
public class OpSweeperSkillCondition implements ISkillCondition
{
	public OpSweeperSkillCondition(StatsSet params)
	{
	
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		AtomicBoolean canSweep = new AtomicBoolean(false);
		if (caster.getActingPlayer() != null)
		{
			final PlayerInstance sweeper = caster.getActingPlayer();
			if (skill != null)
			{
				skill.forEachTargetAffected(sweeper, target, o ->
				{
					if (o.isAttackable())
					{
						Attackable a = (Attackable) o;
						if (a.isDead())
						{
							if (a.isSpoiled())
							{
								canSweep.set(a.checkSpoilOwner(sweeper, true));
								if (canSweep.get())
								{
									canSweep.set(!a.isOldCorpse(sweeper, Config.CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY, true));
								}
								if (canSweep.get())
								{
									canSweep.set(sweeper.getInventory().checkInventorySlotsAndWeight(a.getSpoilLootItems(), true, true));
								}
							}
							else
							{
								sweeper.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
							}
						}
					}
				});
			}
		}
		return canSweep.get();
	}
}
