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
package org.l2junity.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;

public class L2FortDoormenInstance extends L2DoormenInstance
{
	public L2FortDoormenInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2FortDoormenInstance);
	}
	
	@Override
	public void showChatWindow(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (!isOwnerClan(player))
		{
			html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + "-no.htm");
		}
		else if (isUnderSiege())
		{
			html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + "-busy.htm");
		}
		else
		{
			html.setFile(player.getHtmlPrefix(), "data/html/doormen/" + getTemplate().getId() + ".htm");
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	protected final void openDoors(PlayerInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			getFort().openDoor(player, Integer.parseInt(st.nextToken()));
		}
	}
	
	@Override
	protected final void closeDoors(PlayerInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			getFort().closeDoor(player, Integer.parseInt(st.nextToken()));
		}
	}
	
	@Override
	protected final boolean isOwnerClan(PlayerInstance player)
	{
		if ((player.getClan() != null) && (getFort() != null) && (getFort().getOwnerClan() != null))
		{
			if (player.getClanId() == getFort().getOwnerClan().getId())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected final boolean isUnderSiege()
	{
		return getFort().getZone().isActive();
	}
}