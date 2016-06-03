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
import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.items.enchant.EnchantScroll;
import org.l2junity.gameserver.model.items.enchant.EnchantSupportItem;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads item enchant data.
 * @author UnAfraid
 */
public class EnchantItemData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EnchantItemData.class);
	
	private final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
	private final Map<Integer, EnchantSupportItem> _supports = new HashMap<>();
	
	/**
	 * Instantiates a new enchant item data.
	 */
	public EnchantItemData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_scrolls.clear();
		_supports.clear();
		parseDatapackFile("data/enchantItemData.xml");
		LOGGER.info("Loaded {} Enchant Scrolls.", _scrolls.size());
		LOGGER.info("Loaded {} Support Items.", _supports.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		StatsSet set;
		Node att;
		NamedNodeMap attrs;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchant".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						try
						{
							final EnchantScroll item = new EnchantScroll(set);
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("item".equalsIgnoreCase(cd.getNodeName()))
								{
									item.addItem(parseInteger(cd.getAttributes(), "id"));
								}
							}
							_scrolls.put(item.getId(), item);
						}
						catch (NullPointerException e)
						{
							LOGGER.warn("Unexistent enchant scroll: {} defined in enchant data!", set.getString("id"));
						}
						catch (IllegalAccessError e)
						{
							LOGGER.warn("Wrong enchant scroll item type: {} defined in enchant data!", set.getString("id"));
						}
					}
					else if ("support".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						try
						{
							final EnchantSupportItem item = new EnchantSupportItem(set);
							_supports.put(item.getId(), item);
						}
						catch (NullPointerException e)
						{
							LOGGER.warn("Unexistent enchant support item: {} defined in enchant data!", set.getString("id"));
						}
						catch (IllegalAccessError e)
						{
							LOGGER.warn("Wrong enchant support item type: {} defined in enchant data!", set.getString("id"));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the enchant scroll.
	 * @param scroll the scroll
	 * @return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(ItemInstance scroll)
	{
		return _scrolls.get(scroll.getId());
	}
	
	/**
	 * Gets the support item.
	 * @param item the item
	 * @return enchant template for support item
	 */
	public final EnchantSupportItem getSupportItem(ItemInstance item)
	{
		return _supports.get(item.getId());
	}
	
	/**
	 * Gets the single instance of EnchantItemData.
	 * @return single instance of EnchantItemData
	 */
	public static final EnchantItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemData _instance = new EnchantItemData();
	}
}
