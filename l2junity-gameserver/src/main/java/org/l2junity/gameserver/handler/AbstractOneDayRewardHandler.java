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
package org.l2junity.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.enums.OneDayRewardStatus;
import org.l2junity.gameserver.model.OneDayRewardDataHolder;
import org.l2junity.gameserver.model.OneDayRewardPlayerEntry;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.ListenersContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sdw
 */
public abstract class AbstractOneDayRewardHandler extends ListenersContainer
{
	protected Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private final Map<Integer, OneDayRewardPlayerEntry> _entries = new ConcurrentHashMap<>();
	private final OneDayRewardDataHolder _holder;
	
	protected AbstractOneDayRewardHandler(OneDayRewardDataHolder holder)
	{
		_holder = holder;
		init();
	}
	
	public OneDayRewardDataHolder getHolder()
	{
		return _holder;
	}
	
	public abstract boolean isAvailable(PlayerInstance player);
	
	public abstract void init();
	
	public int getStatus(PlayerInstance player)
	{
		final OneDayRewardPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getStatus().getClientId() : OneDayRewardStatus.NOT_AVAILABLE.getClientId();
	}
	
	public int getProgress(PlayerInstance player)
	{
		final OneDayRewardPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getProgress() : 0;
	}
	
	public synchronized void reset()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_daily_rewards WHERE rewardId = ? AND status = ?"))
		{
			ps.setInt(1, _holder.getId());
			ps.setInt(2, OneDayRewardStatus.COMPLETED.getClientId());
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warn("Error while clearing data for: {}", getClass().getSimpleName(), e);
		}
		finally
		{
			_entries.clear();
		}
	}
	
	public boolean requestReward(PlayerInstance player)
	{
		if (isAvailable(player))
		{
			giveRewards(player);
			
			final OneDayRewardPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
			entry.setStatus(OneDayRewardStatus.COMPLETED);
			entry.setLastCompleted(System.currentTimeMillis());
			storePlayerEntry(entry);
			
			return true;
		}
		return false;
	}
	
	protected void giveRewards(PlayerInstance player)
	{
		_holder.getRewards().forEach(i -> player.addItem("One Day Reward", i, player, true));
	}
	
	protected void storePlayerEntry(OneDayRewardPlayerEntry entry)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_daily_rewards (charId, rewardId, status, progress, lastCompleted) VALUES (?, ?, ?, ?, ?)"))
		{
			ps.setInt(1, entry.getObjectId());
			ps.setInt(2, entry.getRewardId());
			ps.setInt(3, entry.getStatus().getClientId());
			ps.setInt(4, entry.getProgress());
			ps.setLong(5, entry.getLastCompleted());
			ps.execute();
			
			// Cache if not exists
			_entries.computeIfAbsent(entry.getObjectId(), id -> entry);
		}
		catch (Exception e)
		{
			LOGGER.warn("Error while saving reward {} for player: {} in database: ", entry.getRewardId(), entry.getObjectId(), e);
		}
	}
	
	protected OneDayRewardPlayerEntry getPlayerEntry(int objectId, boolean createIfNone)
	{
		final OneDayRewardPlayerEntry existingEntry = _entries.get(objectId);
		if (existingEntry != null)
		{
			return existingEntry;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM character_daily_rewards WHERE charId = ? AND rewardId = ?"))
		{
			ps.setInt(1, objectId);
			ps.setInt(2, _holder.getId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					final OneDayRewardPlayerEntry entry = new OneDayRewardPlayerEntry(rs.getInt("charId"), rs.getInt("rewardId"), rs.getInt("status"), rs.getInt("progress"), rs.getLong("lastCompleted"));
					_entries.put(objectId, entry);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Error while loading reward {} for player: {} in database: ", _holder.getId(), objectId, e);
		}
		
		if (createIfNone)
		{
			final OneDayRewardPlayerEntry entry = new OneDayRewardPlayerEntry(objectId, _holder.getId());
			_entries.put(objectId, entry);
			return entry;
		}
		return null;
	}
}
