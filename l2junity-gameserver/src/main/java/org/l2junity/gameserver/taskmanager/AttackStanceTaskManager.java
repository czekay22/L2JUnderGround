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
package org.l2junity.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.network.client.send.AutoAttackStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attack stance task manager.
 * @author Luca Baldi, Zoey76
 */
public class AttackStanceTaskManager
{
	protected static final Logger _log = LoggerFactory.getLogger(AttackStanceTaskManager.class);
	
	protected static final Map<Creature, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	/**
	 * Instantiates a new attack stance task manager.
	 */
	protected AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	/**
	 * Adds the attack stance task.
	 * @param actor the actor
	 */
	public void addAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			// if (actor.isPlayable())
			// {
			// final PlayerInstance player = actor.getActingPlayer();
			// for (L2CubicInstance cubic : player.getCubics().values())
			// {
			// if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
			// {
			// cubic.doAction();
			// }
			// }
			// }
			_attackStanceTasks.put(actor, System.currentTimeMillis());
		}
	}
	
	/**
	 * Removes the attack stance task.
	 * @param actor the actor
	 */
	public void removeAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			if (actor.isSummon())
			{
				actor = actor.getActingPlayer();
			}
			_attackStanceTasks.remove(actor);
		}
	}
	
	/**
	 * Checks for attack stance task.<br>
	 * @param actor the actor
	 * @return {@code true} if the character has an attack stance task, {@code false} otherwise
	 */
	public boolean hasAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			if (actor.isSummon())
			{
				actor = actor.getActingPlayer();
			}
			return _attackStanceTasks.containsKey(actor);
		}
		return false;
	}
	
	protected class FightModeScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			try
			{
				final Iterator<Entry<Creature, Long>> iter = _attackStanceTasks.entrySet().iterator();
				Entry<Creature, Long> e;
				Creature actor;
				while (iter.hasNext())
				{
					e = iter.next();
					if ((current - e.getValue()) > 15000)
					{
						actor = e.getKey();
						if (actor != null)
						{
							actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
							actor.getAI().setAutoAttacking(false);
							if (actor.isPlayer() && actor.hasSummon())
							{
								final Summon pet = actor.getPet();
								if (pet != null)
								{
									pet.broadcastPacket(new AutoAttackStop(pet.getObjectId()));
								}
								actor.getServitors().values().forEach(s -> s.broadcastPacket(new AutoAttackStop(s.getObjectId())));
							}
						}
						iter.remove();
					}
				}
			}
			catch (Exception e)
			{
				// Unless caught here, players remain in attack positions.
				_log.warn("Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Gets the single instance of AttackStanceTaskManager.
	 * @return single instance of AttackStanceTaskManager
	 */
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}
