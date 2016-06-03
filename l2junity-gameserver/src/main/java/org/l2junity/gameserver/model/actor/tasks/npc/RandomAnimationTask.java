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
package org.l2junity.gameserver.model.actor.tasks.npc;

import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.model.actor.Npc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nik
 */
public class RandomAnimationTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(RandomAnimationTask.class);
	private final Npc _npc;
	private boolean _stopTask;
	
	public RandomAnimationTask(Npc npc)
	{
		_npc = npc;
	}
	
	@Override
	public void run()
	{
		if (_stopTask)
		{
			return;
		}
		
		try
		{
			if (!_npc.isInActiveRegion())
			{
				return;
			}
			
			// Cancel further animation timers until intention is changed to ACTIVE again.
			if (_npc.isAttackable() && (_npc.getAI().getIntention() != AI_INTENTION_ACTIVE))
			{
				return;
			}
			
			if (!_npc.isDead() && !_npc.hasBlockActions())
			{
				_npc.onRandomAnimation(Rnd.get(2, 3));
			}
			
			startRandomAnimationTimer();
		}
		catch (Exception e)
		{
			_log.error("Execution of RandomAnimationTask has failed.", e);
		}
	}
	
	/**
	 * Create a RandomAnimation Task that will be launched after the calculated delay.
	 */
	public void startRandomAnimationTimer()
	{
		if (!_npc.hasRandomAnimation() || _stopTask)
		{
			return;
		}
		
		int minWait = _npc.isAttackable() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		int maxWait = _npc.isAttackable() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
		
		// Calculate the delay before the next animation
		int interval = Rnd.get(minWait, maxWait) * 1000;
		
		// Create a RandomAnimation Task that will be launched after the calculated delay
		ThreadPoolManager.getInstance().scheduleGeneral(this, interval);
	}
	
	/**
	 * Stops the task from continuing and blocks it from continuing ever again. You need to create new task if you want to start it again.
	 */
	public void stopRandomAnimationTimer()
	{
		_stopTask = true;
	}
}
