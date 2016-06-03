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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.l2junity.Config;
import org.l2junity.commons.util.file.filter.NumericNameFilter;
import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.multisell.Entry;
import org.l2junity.gameserver.model.multisell.Ingredient;
import org.l2junity.gameserver.model.multisell.ListContainer;
import org.l2junity.gameserver.model.multisell.PreparedListContainer;
import org.l2junity.gameserver.network.client.send.MultiSellList;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class MultisellData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MultisellData.class);
	
	private final Map<Integer, ListContainer> _entries = new HashMap<>();
	
	public static final int PAGE_SIZE = 40;
	// Special IDs.
	public static final int PC_BANG_POINTS = -100;
	public static final int CLAN_REPUTATION = -200;
	public static final int FAME = -300;
	public static final int FIELD_CYCLE_POINTS = -400;
	public static final int RAIDBOSS_POINTS = -500;
	// Misc
	private static final FileFilter NUMERIC_FILTER = new NumericNameFilter();
	
	protected MultisellData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_entries.clear();
		parseDatapackDirectory("data/multisell", false);
		if (Config.CUSTOM_MULTISELL_LOAD)
		{
			parseDatapackDirectory("data/multisell/custom", false);
		}
		
		verify();
		LOGGER.info("Loaded {} multisell lists.", _entries.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			int id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
			int entryId = 1;
			Node att;
			final ListContainer list = new ListContainer(id);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					list.setApplyTaxes(parseBoolean(n.getAttributes(), "applyTaxes", false));
					list.setNewMultisell(parseBoolean(n.getAttributes(), "isNewMultisell", false));
					list.setMaintainEnchantment(parseBoolean(n.getAttributes(), "maintainEnchantment", false));
					
					att = n.getAttributes().getNamedItem("useRate");
					if (att != null)
					{
						try
						{
							
							list.setUseRate(Double.valueOf(att.getNodeValue()));
							if (list.getUseRate() <= 1e-6)
							{
								throw new NumberFormatException("The value cannot be 0"); // threat 0 as invalid value
							}
						}
						catch (NumberFormatException e)
						{
							
							try
							{
								list.setUseRate(Config.class.getField(att.getNodeValue()).getDouble(Config.class));
							}
							catch (Exception e1)
							{
								LOGGER.warn(e1.getMessage() + doc.getLocalName());
								list.setUseRate(1.0);
							}
							
						}
						catch (DOMException e)
						{
							LOGGER.warn(e.getMessage() + doc.getLocalName());
						}
					}
					
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							Entry e = parseEntry(d, entryId++, list);
							list.getEntries().add(e);
						}
						else if ("npcs".equalsIgnoreCase(d.getNodeName()))
						{
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("npc".equalsIgnoreCase(b.getNodeName()))
								{
									if (Util.isDigit(b.getTextContent()))
									{
										list.allowNpc(Integer.parseInt(b.getTextContent()));
									}
								}
							}
						}
					}
				}
			}
			_entries.put(id, list);
		}
		catch (Exception e)
		{
			LOGGER.error("Error in file: {}", f, e);
		}
	}
	
	@Override
	public FileFilter getCurrentFileFilter()
	{
		return NUMERIC_FILTER;
	}
	
	private final Entry parseEntry(Node n, int entryId, ListContainer list)
	{
		Node first = n.getFirstChild();
		final Entry entry = new Entry(entryId);
		
		NamedNodeMap attrs;
		Node att;
		StatsSet set;
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				attrs = n.getAttributes();
				set = new StatsSet();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					att = attrs.item(i);
					set.set(att.getNodeName(), att.getNodeValue());
				}
				entry.addIngredient(new Ingredient(set));
			}
			else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				attrs = n.getAttributes();
				set = new StatsSet();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					att = attrs.item(i);
					set.set(att.getNodeName(), att.getNodeValue());
				}
				entry.addProduct(new Ingredient(set));
			}
		}
		
		return entry;
	}
	
	/**
	 * This will generate the multisell list for the items.<br>
	 * There exist various parameters in multisells that affect the way they will appear:
	 * <ol>
	 * <li>Inventory only:
	 * <ul>
	 * <li>If true, only show items of the multisell for which the "primary" ingredients are already in the player's inventory. By "primary" ingredients we mean weapon and armor.</li>
	 * <li>If false, show the entire list.</li>
	 * </ul>
	 * </li>
	 * <li>Maintain enchantment: presumably, only lists with "inventory only" set to true should sometimes have this as true. This makes no sense otherwise...
	 * <ul>
	 * <li>If true, then the product will match the enchantment level of the ingredient.<br>
	 * If the player has multiple items that match the ingredient list but the enchantment levels differ, then the entries need to be duplicated to show the products and ingredients for each enchantment level.<br>
	 * For example: If the player has a crystal staff +1 and a crystal staff +3 and goes to exchange it at the mammon, the list should have all exchange possibilities for the +1 staff, followed by all possibilities for the +3 staff.</li>
	 * <li>If false, then any level ingredient will be considered equal and product will always be at +0</li>
	 * </ul>
	 * </li>
	 * <li>Apply taxes: Uses the "taxIngredient" entry in order to add a certain amount of adena to the ingredients.
	 * <li>
	 * <li>Additional product and ingredient multipliers.</li>
	 * </ol>
	 * @param listId
	 * @param player
	 * @param npc
	 * @param inventoryOnly
	 * @param productMultiplier
	 * @param ingredientMultiplier
	 */
	public final void separateAndSend(int listId, PlayerInstance player, Npc npc, boolean inventoryOnly, double productMultiplier, double ingredientMultiplier)
	{
		ListContainer template = _entries.get(listId);
		if (template == null)
		{
			LOGGER.warn("Can't find list id: {} requested by player: {}, npcId: {}", listId, player.getName(), (npc != null ? npc.getId() : 0));
			return;
		}
		
		if (((npc != null) && !template.isNpcAllowed(npc.getId())) || ((npc == null) && template.isNpcOnly()))
		{
			LOGGER.warn("Player {} attempted to open multisell {} from npc {} which is not allowed!", player, listId, npc);
			return;
		}
		
		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		
		// Pass through this only when multipliers are different from 1
		if ((productMultiplier != 1) || (ingredientMultiplier != 1))
		{
			list.getEntries().forEach(entry ->
			{
				// Math.max used here to avoid dropping count to 0
				entry.getProducts().forEach(product -> product.setItemCount((long) Math.max(product.getItemCount() * productMultiplier, 1)));
				
				// Math.max used here to avoid dropping count to 0
				entry.getIngredients().forEach(ingredient -> ingredient.setItemCount((long) Math.max(ingredient.getItemCount() * ingredientMultiplier, 1)));
			});
		}
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());
		
		player.setMultiSell(list);
	}
	
	public final void separateAndSend(int listId, PlayerInstance player, Npc npc, boolean inventoryOnly)
	{
		separateAndSend(listId, player, npc, inventoryOnly, 1, 1);
	}
	
	public static final boolean hasSpecialIngredient(int id, long amount, PlayerInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
			{
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
					return false;
				}
				else if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return false;
				}
				else if (player.getClan().getReputationScore() < amount)
				{
					player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_IS_TOO_LOW);
					return false;
				}
				return true;
			}
			case FAME:
			{
				if (player.getFame() < amount)
				{
					player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_FAME_TO_DO_THAT);
					return false;
				}
				return true;
			}
			case RAIDBOSS_POINTS:
			{
				if (player.getRaidbossPoints() < amount)
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_RAID_POINTS);
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	public static final boolean takeSpecialIngredient(int id, long amount, PlayerInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
			{
				player.getClan().takeReputationScore((int) amount, true);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_POINT_S_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION);
				smsg.addLong(amount);
				player.sendPacket(smsg);
				return true;
			}
			case FAME:
			{
				player.setFame(player.getFame() - (int) amount);
				player.sendPacket(new UserInfo(player));
				// player.sendPacket(new ExBrExtraUserInfo(player));
				return true;
			}
			case RAIDBOSS_POINTS:
			{
				player.setRaidbossPoints(player.getRaidbossPoints() - (int) amount);
				player.sendPacket(new UserInfo(player));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CONSUMED_S1_RAID_POINTS).addLong(amount));
				return true;
			}
		}
		return false;
	}
	
	public static final void giveSpecialProduct(int id, long amount, PlayerInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
			{
				player.getClan().addReputationScore((int) amount, true);
				break;
			}
			case FAME:
			{
				player.setFame((int) (player.getFame() + amount));
				player.sendPacket(new UserInfo(player));
				// player.sendPacket(new ExBrExtraUserInfo(player));
				break;
			}
			case RAIDBOSS_POINTS:
			{
				player.increaseRaidbossPoints((int) amount);
				player.sendPacket(new UserInfo(player));
				break;
			}
		}
	}
	
	private final void verify()
	{
		ListContainer list;
		final Iterator<ListContainer> iter = _entries.values().iterator();
		while (iter.hasNext())
		{
			list = iter.next();
			
			for (Entry ent : list.getEntries())
			{
				for (Ingredient ing : ent.getIngredients())
				{
					if (!verifyIngredient(ing))
					{
						LOGGER.warn("can't find ingredient with itemId: {} in list: {}", ing.getItemId(), list.getListId());
					}
				}
				for (Ingredient ing : ent.getProducts())
				{
					if (!verifyIngredient(ing))
					{
						LOGGER.warn("can't find product with itemId: {} in list: {}", ing.getItemId(), list.getListId());
					}
				}
			}
		}
	}
	
	private final boolean verifyIngredient(Ingredient ing)
	{
		switch (ing.getItemId())
		{
			case CLAN_REPUTATION:
			case FAME:
			case RAIDBOSS_POINTS:
				return true;
			default:
				return ing.getTemplate() != null;
		}
	}
	
	public static MultisellData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MultisellData _instance = new MultisellData();
	}
}
