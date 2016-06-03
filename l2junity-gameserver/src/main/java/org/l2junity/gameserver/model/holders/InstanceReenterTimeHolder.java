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
package org.l2junity.gameserver.model.holders;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

/**
 * Simple class for storing Reenter Data for Instances.
 * @author FallenAngel
 */
public final class InstanceReenterTimeHolder
{
	private DayOfWeek _day = null;
	private int _hour = -1;
	private int _minute = -1;
	private long _time = -1;
	
	public InstanceReenterTimeHolder(int time)
	{
		_time = TimeUnit.MINUTES.toMillis(time);
	}
	
	public InstanceReenterTimeHolder(DayOfWeek day, int hour, int minute)
	{
		_day = day;
		_hour = hour;
		_minute = minute;
	}
	
	public final long getTime()
	{
		return _time;
	}
	
	public final DayOfWeek getDay()
	{
		return _day;
	}
	
	public final int getHour()
	{
		return _hour;
	}
	
	public final int getMinute()
	{
		return _minute;
	}
}