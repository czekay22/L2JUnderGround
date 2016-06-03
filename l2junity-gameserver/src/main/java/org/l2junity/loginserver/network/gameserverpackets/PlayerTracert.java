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
package org.l2junity.loginserver.network.gameserverpackets;

import org.l2junity.Config;
import org.l2junity.loginserver.LoginController;
import org.l2junity.util.network.BaseRecievePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mrTJO
 */
public class PlayerTracert extends BaseRecievePacket
{
	protected static Logger _log = LoggerFactory.getLogger(PlayerTracert.class);
	
	/**
	 * @param decrypt
	 */
	public PlayerTracert(byte[] decrypt)
	{
		super(decrypt);
		String account = readS();
		String pcIp = readS();
		String hop1 = readS();
		String hop2 = readS();
		String hop3 = readS();
		String hop4 = readS();
		
		LoginController.getInstance().setAccountLastTracert(account, pcIp, hop1, hop2, hop3, hop4);
		if (Config.DEBUG)
		{
			_log.info("Saved " + account + " last tracert");
		}
	}
}
