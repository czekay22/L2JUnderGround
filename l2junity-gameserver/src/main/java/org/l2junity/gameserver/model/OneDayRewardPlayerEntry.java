/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.gameserver.model;

import org.l2junity.gameserver.enums.OneDayRewardStatus;

/**
 * @author UnAfraid
 */
public class OneDayRewardPlayerEntry
{
	private final int _objectId;
	private final int _rewardId;
	private OneDayRewardStatus _status = OneDayRewardStatus.NOT_AVAILABLE;
	private int _progress;
	private long _lastCompleted;
	
	public OneDayRewardPlayerEntry(int objectId, int rewardId)
	{
		_objectId = objectId;
		_rewardId = rewardId;
	}
	
	public OneDayRewardPlayerEntry(int objectId, int rewardId, int status, int progress, long lastCompleted)
	{
		this(objectId, rewardId);
		_status = OneDayRewardStatus.valueOf(status);
		_progress = progress;
		_lastCompleted = lastCompleted;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public int getRewardId()
	{
		return _rewardId;
	}
	
	public OneDayRewardStatus getStatus()
	{
		return _status;
	}
	
	public void setStatus(OneDayRewardStatus status)
	{
		_status = status;
	}
	
	public int getProgress()
	{
		return _progress;
	}
	
	public void setProgress(int progress)
	{
		_progress = progress;
	}
	
	public int increaseProgress()
	{
		_progress++;
		return _progress;
	}
	
	public long getLastCompleted()
	{
		return _lastCompleted;
	}
	
	public void setLastCompleted(long lastCompleted)
	{
		_lastCompleted = lastCompleted;
	}
}
