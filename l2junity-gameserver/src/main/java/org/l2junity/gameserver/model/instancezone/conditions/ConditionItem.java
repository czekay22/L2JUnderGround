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
package org.l2junity.gameserver.model.instancezone.conditions;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.instancezone.InstanceTemplate;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Instance item condition
 * @author malyelfik
 */
public final class ConditionItem extends Condition
{
	private final int _itemId;
	private final long _count;
	private final boolean _take;
	
	public ConditionItem(InstanceTemplate template, StatsSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		// Load params
		_itemId = parameters.getInt("id");
		_count = parameters.getLong("count");
		_take = parameters.getBoolean("take", false);
		// Set message
		setSystemMessage(SystemMessageId.C1_S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED, (msg, player) -> msg.addCharName(player));
	}
	
	@Override
	protected boolean test(PlayerInstance player, Npc npc)
	{
		return player.getInventory().getInventoryItemCount(_itemId, -1) >= _count;
	}
	
	@Override
	protected void onSuccess(PlayerInstance player)
	{
		if (_take)
		{
			player.destroyItemByItemId("InstanceConditionDestroy", _itemId, _count, null, true);
		}
	}
}