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
package org.l2junity.commons.util;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to check for deadlocked threads.
 * @author -Nemesiss- L2M
 */
public class DeadLockDetector extends Thread
{
	private static Logger LOGGER = LoggerFactory.getLogger(DeadLockDetector.class);
	
	private final Duration _checkInterval;
	private final Runnable _deadLockCallback;
	private final ThreadMXBean tmx;
	
	public DeadLockDetector(Duration checkInterval, Runnable deadLockCallback)
	{
		super("DeadLockDetector");
		_checkInterval = checkInterval;
		_deadLockCallback = deadLockCallback;
		tmx = ManagementFactory.getThreadMXBean();
	}
	
	@Override
	public final void run()
	{
		boolean deadlock = false;
		while (!deadlock)
		{
			try
			{
				final long[] ids = tmx.findDeadlockedThreads();
				
				if (ids != null)
				{
					deadlock = true;
					ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
					StringBuilder info = new StringBuilder();
					info.append("DeadLock Found!");
					info.append(System.lineSeparator());
					for (ThreadInfo ti : tis)
					{
						info.append(ti.toString());
					}
					
					for (ThreadInfo ti : tis)
					{
						LockInfo[] locks = ti.getLockedSynchronizers();
						MonitorInfo[] monitors = ti.getLockedMonitors();
						if ((locks.length == 0) && (monitors.length == 0))
						{
							continue;
						}
						
						ThreadInfo dl = ti;
						info.append("Java-level deadlock:");
						info.append(System.lineSeparator());
						info.append('\t');
						info.append(dl.getThreadName());
						info.append(" is waiting to lock ");
						info.append(dl.getLockInfo().toString());
						info.append(" which is held by ");
						info.append(dl.getLockOwnerName());
						info.append(System.lineSeparator());
						while ((dl = tmx.getThreadInfo(new long[]
						{
							dl.getLockOwnerId()
						}, true, true)[0]).getThreadId() != ti.getThreadId())
						{
							info.append('\t');
							info.append(dl.getThreadName());
							info.append(" is waiting to lock ");
							info.append(dl.getLockInfo().toString());
							info.append(" which is held by ");
							info.append(dl.getLockOwnerName());
							info.append(System.lineSeparator());
						}
					}
					
					LOGGER.warn(info.toString());
					
					if (_deadLockCallback != null)
					{
						_deadLockCallback.run();
					}
				}
				Thread.sleep(_checkInterval.toMillis());
			}
			catch (Exception e)
			{
				LOGGER.warn("", e);
			}
		}
	}
}
