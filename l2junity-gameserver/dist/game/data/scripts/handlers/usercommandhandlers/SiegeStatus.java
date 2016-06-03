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
package handlers.usercommandhandlers;

import org.l2junity.gameserver.handler.IUserCommandHandler;
import org.l2junity.gameserver.instancemanager.SiegeManager;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Siege;
import org.l2junity.gameserver.model.zone.type.SiegeZone;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * @author Tryskell
 */
public class SiegeStatus implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		99
	};
	
	private static final String INSIDE_SIEGE_ZONE = "Castle Siege in Progress";
	private static final String OUTSIDE_SIEGE_ZONE = "No Castle Siege Area";
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if (!activeChar.isNoble() || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_EXALTED_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR);
			return false;
		}
		
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (!siege.isInProgress())
			{
				continue;
			}
			
			final L2Clan clan = activeChar.getClan();
			if (!siege.checkIsAttacker(clan) && !siege.checkIsDefender(clan))
			{
				continue;
			}
			
			final SiegeZone siegeZone = siege.getCastle().getZone();
			final StringBuilder sb = new StringBuilder();
			for (PlayerInstance member : clan.getOnlineMembers(0))
			{
				sb.append("<tr><td width=170>");
				sb.append(member.getName());
				sb.append("</td><td width=100>");
				sb.append(siegeZone.isInsideZone(member) ? INSIDE_SIEGE_ZONE : OUTSIDE_SIEGE_ZONE);
				sb.append("</td></tr>");
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage();
			html.setFile(activeChar.getHtmlPrefix(), "data/html/siege/siege_status.htm");
			html.replace("%kill_count%", clan.getSiegeKills());
			html.replace("%death_count%", clan.getSiegeDeaths());
			html.replace("%member_list%", sb.toString());
			activeChar.sendPacket(html);
			
			return true;
		}
		
		activeChar.sendPacket(SystemMessageId.ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_EXALTED_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR);
		
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
	
}
