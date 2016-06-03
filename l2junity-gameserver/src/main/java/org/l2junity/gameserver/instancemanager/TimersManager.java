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
package org.l2junity.gameserver.instancemanager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.timers.TimerHolder;

/**
 * @author UnAfraid
 */
public class TimersManager
{
	private final Map<Integer, Set<TimerHolder<?>>> _timers = new ConcurrentHashMap<>();
	
	public void registerTimer(TimerHolder<?> timer)
	{
		final Npc npc = timer.getNpc();
		if (npc != null)
		{
			_timers.computeIfAbsent(npc.getObjectId(), key -> ConcurrentHashMap.newKeySet()).add(timer);
		}
		
		final PlayerInstance player = timer.getPlayer();
		if (player != null)
		{
			_timers.computeIfAbsent(player.getObjectId(), key -> ConcurrentHashMap.newKeySet()).add(timer);
		}
	}
	
	public void cancelTimers(int objectId)
	{
		final Set<TimerHolder<?>> timers = _timers.remove(objectId);
		if (timers != null)
		{
			timers.forEach(TimerHolder::cancelTimer);
		}
	}
	
	public static TimersManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TimersManager _instance = new TimersManager();
	}
}
