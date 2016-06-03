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

package org.l2junity.gameserver.network.client.recv;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * Lets drink to code!
 * @author zabbix, HorridoJoho
 */
public final class RequestLinkHtml implements IClientIncomingPacket
{
	private String _link;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_link = packet.readS();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance actor = client.getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (_link.isEmpty())
		{
			_log.warn("Player " + actor.getName() + " sent empty html link!");
			return;
		}
		
		if (_link.contains(".."))
		{
			_log.warn("Player " + actor.getName() + " sent invalid html link: link " + _link);
			return;
		}
		
		int htmlObjectId = actor.validateHtmlAction("link " + _link);
		if (htmlObjectId == -1)
		{
			_log.warn("Player " + actor.getName() + " sent non cached  html link: link " + _link);
			return;
		}
		
		if ((htmlObjectId > 0) && !Util.isInsideRangeOfObjectId(actor, htmlObjectId, Npc.INTERACTION_DISTANCE))
		{
			// No logging here, this could be a common case
			return;
		}
		
		String filename = "data/html/" + _link;
		final NpcHtmlMessage msg = new NpcHtmlMessage(htmlObjectId);
		msg.setFile(actor.getHtmlPrefix(), filename);
		actor.sendPacket(msg);
		
		if (actor.isGM() && actor.isDebug())
		{
			actor.sendMessage("HTML: " + filename);
		}
	}
}
