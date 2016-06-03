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
package org.l2junity.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.model.punishment.PunishmentType;

/**
 * This class manages handlers of punishments.
 * @author UnAfraid
 */
public class PunishmentHandler implements IHandler<IPunishmentHandler, PunishmentType>
{
	private final Map<PunishmentType, IPunishmentHandler> _handlers = new HashMap<>();
	
	protected PunishmentHandler()
	{
		
	}
	
	@Override
	public void registerHandler(IPunishmentHandler handler)
	{
		_handlers.put(handler.getType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IPunishmentHandler handler)
	{
		_handlers.remove(handler.getType());
	}
	
	@Override
	public IPunishmentHandler getHandler(PunishmentType val)
	{
		return _handlers.get(val);
	}
	
	@Override
	public int size()
	{
		return _handlers.size();
	}
	
	public static PunishmentHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PunishmentHandler _instance = new PunishmentHandler();
	}
}
