/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.model.events.impl.instance;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.IBaseEvent;
import org.l2junity.gameserver.model.instancezone.Instance;

/**
 * @author malyelfik
 */
public final class OnInstanceCreated implements IBaseEvent
{
	private final Instance _instance;
	private final PlayerInstance _creator;
	
	public OnInstanceCreated(Instance instance, PlayerInstance creator)
	{
		_instance = instance;
		_creator = creator;
	}
	
	public Instance getInstanceWorld()
	{
		return _instance;
	}
	
	public PlayerInstance getCreator()
	{
		return _creator;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_INSTANCE_CREATED;
	}
}