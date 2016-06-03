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
package handlers.effecthandlers;

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.holders.ItemChanceHolder;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class CreateItemRandom extends AbstractEffect
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateItemRandom.class);
	
	public CreateItemRandom(StatsSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return Boolean.TRUE;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		final PlayerInstance player = effected.getActingPlayer();
		if (player == null)
		{
			return;
		}
		else if (item == null)
		{
			LOGGER.warn("{} Attempting to cast skill: {} without item defined!", player, skill);
			return;
		}
		else if (item.getItem().getCreateItems().isEmpty())
		{
			LOGGER.warn("{} Attempting to cast skill: {} with item {} without createItems defined!", player, skill, item);
			return;
		}
		
		ItemChanceHolder selectedItem = null;
		final double random = Rnd.nextDouble() * 100;
		double comulativeChance = 0;
		for (ItemChanceHolder holder : item.getItem().getCreateItems())
		{
			if ((comulativeChance += holder.getChance()) >= random)
			{
				selectedItem = holder;
				break;
			}
		}
		
		if (selectedItem == null)
		{
			player.sendPacket(SystemMessageId.THERE_WAS_NOTHING_FOUND_INSIDE);
			return;
		}
		
		player.addItem("CreateItems", selectedItem.getId(), selectedItem.getCount(), player, true);
	}
}
