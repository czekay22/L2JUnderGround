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
package org.l2junity.gameserver.model.drops;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NosBit
 */
public enum DropListScope
{
	DEATH(DeathDropItem.class, GroupedDeathDropItem.class),
	CORPSE(CorpseDropItem.class, GroupedCorpseDropItem.class);
	
	private static final Logger _log = LoggerFactory.getLogger(DropListScope.class);
	
	private final Class<? extends GeneralDropItem> _dropItemClass;
	private final Class<? extends GroupedGeneralDropItem> _groupedDropItemClass;
	
	DropListScope(Class<? extends GeneralDropItem> dropItemClass, Class<? extends GroupedGeneralDropItem> groupedDropItemClass)
	{
		_dropItemClass = dropItemClass;
		_groupedDropItemClass = groupedDropItemClass;
	}
	
	public IDropItem newDropItem(int itemId, long min, long max, double chance)
	{
		final Constructor<? extends GeneralDropItem> constructor;
		try
		{
			constructor = _dropItemClass.getConstructor(int.class, long.class, long.class, double.class);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			_log.error("Constructor(int, long, long, double) not found for " + _dropItemClass.getSimpleName(), e);
			return null;
		}
		
		try
		{
			return constructor.newInstance(itemId, min, max, chance);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			_log.error("", e);
			return null;
		}
	}
	
	public GroupedGeneralDropItem newGroupedDropItem(double chance)
	{
		final Constructor<? extends GroupedGeneralDropItem> constructor;
		try
		{
			constructor = _groupedDropItemClass.getConstructor(double.class);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			_log.error("Constructor(double) not found for " + _groupedDropItemClass.getSimpleName(), e);
			return null;
		}
		
		try
		{
			return constructor.newInstance(chance);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			_log.error("", e);
			return null;
		}
	}
}
