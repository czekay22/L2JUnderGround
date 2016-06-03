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

import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nBd
 */
public interface IBypassHandler
{
	Logger _log = LoggerFactory.getLogger(IBypassHandler.class);
	
	/**
	 * This is the worker method that is called when someone uses an bypass command.
	 * @param command
	 * @param activeChar
	 * @param bypassOrigin
	 * @return success
	 */
	boolean useBypass(String command, PlayerInstance activeChar, Creature bypassOrigin);
	
	/**
	 * This method is called at initialization to register all bypasses automatically.
	 * @return all known bypasses
	 */
	String[] getBypassList();
}