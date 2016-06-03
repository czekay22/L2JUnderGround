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
package org.l2junity.gameserver.script.faenor;

import java.util.List;

import org.l2junity.gameserver.data.sql.impl.AnnouncementsTable;
import org.l2junity.gameserver.datatables.EventDroplist;
import org.l2junity.gameserver.model.announce.EventAnnouncement;
import org.l2junity.gameserver.script.DateRange;
import org.l2junity.gameserver.script.EngineInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{
	protected static final Logger _log = LoggerFactory.getLogger(FaenorInterface.class);
	
	public static FaenorInterface getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public List<?> getAllPlayers()
	{
		return null;
	}
	
	@Override
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int) (chance * 1000000), range);
	}
	
	@Override
	public void onPlayerLogin(String message, DateRange validDateRange)
	{
		AnnouncementsTable.getInstance().addAnnouncement(new EventAnnouncement(validDateRange, message));
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorInterface _instance = new FaenorInterface();
	}
}
