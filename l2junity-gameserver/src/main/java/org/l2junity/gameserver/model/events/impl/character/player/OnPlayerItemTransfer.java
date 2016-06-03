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
package org.l2junity.gameserver.model.events.impl.character.player;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.IBaseEvent;
import org.l2junity.gameserver.model.itemcontainer.ItemContainer;
import org.l2junity.gameserver.model.items.instance.ItemInstance;

/**
 * @author UnAfraid
 */
public class OnPlayerItemTransfer implements IBaseEvent
{
	private final PlayerInstance _activeChar;
	private final ItemInstance _item;
	private final ItemContainer _container;
	
	public OnPlayerItemTransfer(PlayerInstance activeChar, ItemInstance item, ItemContainer container)
	{
		_activeChar = activeChar;
		_item = item;
		_container = container;
	}
	
	public PlayerInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public ItemInstance getItem()
	{
		return _item;
	}
	
	public ItemContainer getContainer()
	{
		return _container;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_ITEM_TRANSFER;
	}
}
