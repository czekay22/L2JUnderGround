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
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.commons.util.file.filter.NumericNameFilter;
import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.model.buylist.Product;
import org.l2junity.gameserver.model.buylist.ProductList;
import org.l2junity.gameserver.model.items.L2Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads buy lists for NPCs.
 * @author NosBit
 */
public final class BuyListData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BuyListData.class);
	
	private final Map<Integer, ProductList> _buyLists = new HashMap<>();
	private static final FileFilter NUMERIC_FILTER = new NumericNameFilter();
	
	protected BuyListData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_buyLists.clear();
		parseDatapackDirectory("data/buylists", false);
		if (Config.CUSTOM_BUYLIST_LOAD)
		{
			parseDatapackDirectory("data/buylists/custom", false);
		}
		
		LOGGER.info("Loaded {} BuyLists.", _buyLists.size());
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM `buylists`"))
		{
			while (rs.next())
			{
				int buyListId = rs.getInt("buylist_id");
				int itemId = rs.getInt("item_id");
				long count = rs.getLong("count");
				long nextRestockTime = rs.getLong("next_restock_time");
				final ProductList buyList = getBuyList(buyListId);
				if (buyList == null)
				{
					LOGGER.warn("BuyList found in database but not loaded from xml! BuyListId: " + buyListId);
					continue;
				}
				final Product product = buyList.getProductByItemId(itemId);
				if (product == null)
				{
					LOGGER.warn("ItemId found in database but not loaded from xml! BuyListId: " + buyListId + " ItemId: " + itemId);
					continue;
				}
				if (count < product.getMaxCount())
				{
					product.setCount(count);
					product.restartRestockTask(nextRestockTime);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to load buyList data from database.", e);
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			final int buyListId = Integer.parseInt(f.getName().replaceAll(".xml", ""));
			
			for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equalsIgnoreCase(node.getNodeName()))
				{
					final ProductList buyList = new ProductList(buyListId);
					for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
					{
						if ("item".equalsIgnoreCase(list_node.getNodeName()))
						{
							int itemId = -1;
							long price = -1;
							long restockDelay = -1;
							long count = -1;
							NamedNodeMap attrs = list_node.getAttributes();
							Node attr = attrs.getNamedItem("id");
							itemId = Integer.parseInt(attr.getNodeValue());
							attr = attrs.getNamedItem("price");
							if (attr != null)
							{
								price = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("restock_delay");
							if (attr != null)
							{
								restockDelay = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("count");
							if (attr != null)
							{
								count = Long.parseLong(attr.getNodeValue());
							}
							final L2Item item = ItemTable.getInstance().getTemplate(itemId);
							if (item != null)
							{
								buyList.addProduct(new Product(buyList.getListId(), item, price, restockDelay, count));
							}
							else
							{
								LOGGER.warn("Item not found. BuyList:" + buyList.getListId() + " ItemID:" + itemId + " File:" + f.getName());
							}
						}
						else if ("npcs".equalsIgnoreCase(list_node.getNodeName()))
						{
							for (Node npcs_node = list_node.getFirstChild(); npcs_node != null; npcs_node = npcs_node.getNextSibling())
							{
								if ("npc".equalsIgnoreCase(npcs_node.getNodeName()))
								{
									int npcId = Integer.parseInt(npcs_node.getTextContent());
									buyList.addAllowedNpc(npcId);
								}
							}
						}
					}
					_buyLists.put(buyList.getListId(), buyList);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to load buyList data from xml File:" + f.getName(), e);
		}
	}
	
	@Override
	public FileFilter getCurrentFileFilter()
	{
		return NUMERIC_FILTER;
	}
	
	public ProductList getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}
	
	public static BuyListData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BuyListData _instance = new BuyListData();
	}
}
