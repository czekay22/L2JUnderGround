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
package org.l2junity.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.model.L2TeleportLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleportLocationTable
{
	private static Logger LOGGER = LoggerFactory.getLogger(TeleportLocationTable.class);
	
	private final Map<Integer, L2TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleportLocationTable()
	{
		reloadAll();
	}
	
	public void reloadAll()
	{
		_teleports.clear();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM teleport"))
		{
			L2TeleportLocation teleport;
			while (rs.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rs.getInt("id"));
				teleport.setLocX(rs.getInt("loc_x"));
				teleport.setLocY(rs.getInt("loc_y"));
				teleport.setLocZ(rs.getInt("loc_z"));
				teleport.setPrice(rs.getInt("price"));
				teleport.setIsForNoble(rs.getInt("fornoble") == 1);
				teleport.setItemId(rs.getInt("itemId"));
				
				_teleports.put(teleport.getTeleId(), teleport);
			}
			LOGGER.info("Loaded {} Teleport Location Templates.", _teleports.size());
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading Teleport Table.", e);
		}
		
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			int cTeleCount = _teleports.size();
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM custom_teleport"))
			{
				L2TeleportLocation teleport;
				while (rs.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rs.getInt("id"));
					teleport.setLocX(rs.getInt("loc_x"));
					teleport.setLocY(rs.getInt("loc_y"));
					teleport.setLocZ(rs.getInt("loc_z"));
					teleport.setPrice(rs.getInt("price"));
					teleport.setIsForNoble(rs.getInt("fornoble") == 1);
					teleport.setItemId(rs.getInt("itemId"));
					
					_teleports.put(teleport.getTeleId(), teleport);
				}
				cTeleCount = _teleports.size() - cTeleCount;
				if (cTeleCount > 0)
				{
					LOGGER.info("Loaded {} Custom Teleport Location Templates.", cTeleCount);
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Error while creating custom teleport table {}", e.getMessage(), e);
			}
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
	
	public static TeleportLocationTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportLocationTable _instance = new TeleportLocationTable();
	}
}
