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
package org.l2junity.gameserver.data.xml.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.model.SiegeScheduleDate;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author UnAfraid
 */
public class SiegeScheduleData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SiegeScheduleData.class);
	
	private final List<SiegeScheduleDate> _scheduleData = new ArrayList<>();
	
	protected SiegeScheduleData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_scheduleData.clear();
		parseDatapackFile("config/SiegeSchedule.xml");
		LOGGER.info("Loaded: {} siege schedulers.", _scheduleData.size());
		if (_scheduleData.isEmpty())
		{
			_scheduleData.add(new SiegeScheduleDate(new StatsSet()));
			LOGGER.info("Emergency Load: {} default siege schedulers.", _scheduleData.size());
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling())
				{
					switch (cd.getNodeName())
					{
						case "schedule":
						{
							final StatsSet set = new StatsSet();
							final NamedNodeMap attrs = cd.getAttributes();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								Node node = attrs.item(i);
								String key = node.getNodeName();
								String val = node.getNodeValue();
								if ("day".equals(key))
								{
									if (!Util.isDigit(val))
									{
										val = Integer.toString(getValueForField(val));
									}
								}
								set.set(key, val);
							}
							_scheduleData.add(new SiegeScheduleDate(set));
							break;
						}
					}
				}
			}
		}
	}
	
	private int getValueForField(String field)
	{
		try
		{
			return Calendar.class.getField(field).getInt(Calendar.class);
		}
		catch (Exception e)
		{
			LOGGER.warn("", e);
			return -1;
		}
	}
	
	public List<SiegeScheduleDate> getScheduleDates()
	{
		return _scheduleData;
	}
	
	public static final SiegeScheduleData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeScheduleData _instance = new SiegeScheduleData();
	}
	
}
