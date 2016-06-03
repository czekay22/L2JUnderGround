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
package org.l2junity.gameserver.model.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class PlayerVariables extends AbstractVariables
{
	private static final Logger _log = LoggerFactory.getLogger(PlayerVariables.class);
	
	// SQL Queries.
	private static final String SELECT_QUERY = "SELECT * FROM character_variables WHERE charId = ?";
	private static final String DELETE_QUERY = "DELETE FROM character_variables WHERE charId = ?";
	private static final String INSERT_QUERY = "INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)";
	
	// Public variable names
	public static final String HAIR_ACCESSORY_VARIABLE_NAME = "HAIR_ACCESSORY_ENABLED";
	public static final String WORLD_CHAT_VARIABLE_NAME = "WORLD_CHAT_POINTS";
	public static final String VITALITY_ITEMS_USED_VARIABLE_NAME = "VITALITY_ITEMS_USED";
	private static final String ONE_DAY_REWARDS = "ONE_DAY_REWARDS";
	public static final String CEREMONY_OF_CHAOS_PROHIBITED_PENALTIES = "CEREMONY_OF_CHAOS_PENALTIES";
	public static final String ABILITY_POINTS_MAIN_CLASS = "ABILITY_POINTS";
	public static final String ABILITY_POINTS_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS";
	public static final String ABILITY_POINTS_USED_MAIN_CLASS = "ABILITY_POINTS_USED";
	public static final String ABILITY_POINTS_USED_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS_USED";
	public static final String EXTEND_DROP = "EXTEND_DROP";
	
	private final int _objectId;
	
	public PlayerVariables(int objectId)
	{
		_objectId = objectId;
		restoreMe();
	}
	
	@Override
	public boolean restoreMe()
	{
		// Restore previous variables.
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SELECT_QUERY))
		{
			st.setInt(1, _objectId);
			try (ResultSet rset = st.executeQuery())
			{
				while (rset.next())
				{
					set(rset.getString("var"), rset.getString("val"));
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't restore variables for: " + getPlayer(), e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		return true;
	}
	
	@Override
	public boolean storeMe()
	{
		// No changes, nothing to store.
		if (!hasChanges())
		{
			return false;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			// Clear previous entries.
			try (PreparedStatement st = con.prepareStatement(DELETE_QUERY))
			{
				st.setInt(1, _objectId);
				st.execute();
			}
			
			// Insert all variables.
			try (PreparedStatement st = con.prepareStatement(INSERT_QUERY))
			{
				st.setInt(1, _objectId);
				for (Entry<String, Object> entry : getSet().entrySet())
				{
					st.setString(2, entry.getKey());
					st.setString(3, String.valueOf(entry.getValue()));
					st.addBatch();
				}
				st.executeBatch();
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't update variables for: " + getPlayer(), e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		return true;
	}
	
	@Override
	public boolean deleteMe()
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			// Clear previous entries.
			try (PreparedStatement st = con.prepareStatement(DELETE_QUERY))
			{
				st.setInt(1, _objectId);
				st.execute();
			}
			
			// Clear all entries
			getSet().clear();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Couldn't delete variables for: " + getPlayer(), e);
			return false;
		}
		return true;
	}
	
	public PlayerInstance getPlayer()
	{
		return World.getInstance().getPlayer(_objectId);
	}
	
	public void addOneDayReward(int rewardId)
	{
		String result = getString(ONE_DAY_REWARDS, "");
		if (result.isEmpty())
		{
			result = Integer.toString(rewardId);
		}
		else
		{
			result += "," + rewardId;
		}
		set(ONE_DAY_REWARDS, result);
	}
	
	public void removeOneDayReward(int rewardId)
	{
		String result = "";
		String data = getString(ONE_DAY_REWARDS, "");
		for (String s : data.split(","))
		{
			if (s.equals(Integer.toString(rewardId)))
			{
				continue;
			}
			else if (result.isEmpty())
			{
				result = s;
			}
			else
			{
				result += "," + s;
			}
		}
		set(ONE_DAY_REWARDS, result);
	}
	
	public boolean hasOneDayReward(int rewardId)
	{
		String data = getString(ONE_DAY_REWARDS, "");
		for (String s : data.split(","))
		{
			if (s.equals(Integer.toString(rewardId)))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<Integer> getOneDayRewards()
	{
		List<Integer> rewards = null;
		String data = getString(ONE_DAY_REWARDS, "");
		if (!data.isEmpty())
		{
			for (String s : getString(ONE_DAY_REWARDS, "").split(","))
			{
				if (Util.isDigit(s))
				{
					int rewardId = Integer.parseInt(s);
					if (rewards == null)
					{
						rewards = new ArrayList<>();
					}
					rewards.add(rewardId);
				}
			}
		}
		return rewards != null ? rewards : Collections.emptyList();
	}
	
	public void updateExtendDrop(int id, long count)
	{
		String result = "";
		String data = getString(EXTEND_DROP, "");
		if (data.isEmpty())
		{
			result = Integer.toString(id) + "," + Long.toString(count);
		}
		else
		{
			if (data.contains(";"))
			{
				for (String s : data.split(";"))
				{
					String[] drop = s.split(",");
					if (drop[0].equals(Integer.toString(id)))
					{
						s += ";" + drop[0] + "," + Long.toString(count);
						continue;
					}
					
					result += ";" + s;
				}
				result = result.substring(1);
			}
			else
			{
				result = Integer.toString(id) + "," + Long.toString(count);
			}
		}
		set(EXTEND_DROP, result);
	}
	
	public long getExtendDropCount(int id)
	{
		String data = getString(EXTEND_DROP, "");
		for (String s : data.split(";"))
		{
			String[] drop = s.split(",");
			if (drop[0].equals(Integer.toString(id)))
			{
				return Long.parseLong(drop[1]);
			}
		}
		return 0;
	}
}
