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
package org.l2junity.gameserver.model.actor.tasks.npc.trap;

import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2TrapInstance;
import org.l2junity.gameserver.network.client.send.SocialAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trap task.
 * @author Zoey76
 */
public class TrapTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(TrapTask.class);
	private static final int TICK = 1000; // 1s
	private final L2TrapInstance _trap;
	
	public TrapTask(L2TrapInstance trap)
	{
		_trap = trap;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (!_trap.isTriggered())
			{
				if (_trap.hasLifeTime())
				{
					_trap.setRemainingTime(_trap.getRemainingTime() - TICK);
					if (_trap.getRemainingTime() < (_trap.getLifeTime() - 15000))
					{
						_trap.broadcastPacket(new SocialAction(_trap.getObjectId(), 2));
					}
					if (_trap.getRemainingTime() <= 0)
					{
						_trap.triggerTrap(_trap);
						return;
					}
				}
				
				for (Creature target : World.getInstance().getVisibleObjects(_trap, Creature.class))
				{
					if (_trap.checkTarget(target))
					{
						_trap.triggerTrap(target);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(TrapTask.class.getSimpleName() + ": " + e.getMessage());
			_trap.unSummon();
		}
	}
}
