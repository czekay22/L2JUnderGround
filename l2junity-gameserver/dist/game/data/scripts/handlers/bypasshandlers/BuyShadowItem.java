/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.bypasshandlers;

import org.l2junity.gameserver.handler.IBypassHandler;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2MerchantInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;

public class BuyShadowItem implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"BuyShadowItem"
	};
	
	@Override
	public boolean useBypass(String command, PlayerInstance activeChar, Creature target)
	{
		if (!(target instanceof L2MerchantInstance))
		{
			return false;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
		if (activeChar.getLevel() < 40)
		{
			html.setFile(activeChar.getHtmlPrefix(), "data/html/common/shadow_item-lowlevel.htm");
		}
		else if ((activeChar.getLevel() >= 40) && (activeChar.getLevel() < 46))
		{
			html.setFile(activeChar.getHtmlPrefix(), "data/html/common/shadow_item_d.htm");
		}
		else if ((activeChar.getLevel() >= 46) && (activeChar.getLevel() < 52))
		{
			html.setFile(activeChar.getHtmlPrefix(), "data/html/common/shadow_item_c.htm");
		}
		else if (activeChar.getLevel() >= 52)
		{
			html.setFile(activeChar.getHtmlPrefix(), "data/html/common/shadow_item_b.htm");
		}
		html.replace("%objectId%", String.valueOf(target.getObjectId()));
		activeChar.sendPacket(html);
		
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
