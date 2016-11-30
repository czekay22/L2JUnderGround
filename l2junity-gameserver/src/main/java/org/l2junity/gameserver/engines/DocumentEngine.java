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
package org.l2junity.gameserver.engines;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.commons.util.file.filter.XMLFilter;
import org.l2junity.gameserver.engines.items.DocumentItem;
import org.l2junity.gameserver.model.items.L2Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub
 */
public class DocumentEngine
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentEngine.class);
	
	private final List<File> _itemFiles = new LinkedList<>();
	
	public static DocumentEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DocumentEngine()
	{
		hashFiles("data/stats/items", _itemFiles);
		if (Config.CUSTOM_ITEMS_LOAD)
		{
			hashFiles("data/stats/items/custom", _itemFiles);
		}
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			LOGGER.warn("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles(new XMLFilter());
		for (File f : files)
		{
			hash.add(f);
		}
	}
	
	/**
	 * Return created items
	 * @return List of {@link L2Item}
	 */
	public List<L2Item> loadItems()
	{
		List<L2Item> list = new LinkedList<>();
		for (File f : _itemFiles)
		{
			DocumentItem document = new DocumentItem(f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
	
	private static class SingletonHolder
	{
		protected static final DocumentEngine _instance = new DocumentEngine();
	}
}
