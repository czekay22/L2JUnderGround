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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2junity.gameserver.cache.HtmCache;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.instancemanager.MapRegionManager;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.TeleportWhereType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.model.zone.type.SpawnTerritory;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;

/**
 * Small typo fix by Zoey76 24/02/2011
 */
public class AdminZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_zone_check",
		"admin_zone_visual",
		"admin_zone_visual_clear"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		// String val = "";
		// if (st.countTokens() >= 1) {val = st.nextToken();}
		
		if (actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			showHtml(activeChar);
			activeChar.sendMessage("MapRegion: x:" + MapRegionManager.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionManager.getInstance().getMapRegionY(activeChar.getY()) + " (" + MapRegionManager.getInstance().getMapRegionLocId(activeChar) + ")");
			getGeoRegionXY(activeChar);
			activeChar.sendMessage("Closest Town: " + MapRegionManager.getInstance().getClosestTownName(activeChar));
			
			Location loc;
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
			activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);
			activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGEFLAG);
			activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
			activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
		}
		else if (actualCommand.equalsIgnoreCase("admin_zone_visual"))
		{
			String next = st.nextToken();
			if (next.equalsIgnoreCase("all"))
			{
				for (ZoneType zone : ZoneManager.getInstance().getZones(activeChar))
				{
					zone.visualizeZone(activeChar.getZ());
				}
				for (SpawnTerritory territory : ZoneManager.getInstance().getSpawnTerritories(activeChar))
				{
					territory.visualizeZone(activeChar.getZ());
				}
				showHtml(activeChar);
			}
			else
			{
				int zoneId = Integer.parseInt(next);
				ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_zone_visual_clear"))
		{
			ZoneManager.getInstance().clearDebugItems();
			showHtml(activeChar);
		}
		return true;
	}
	
	private static void showHtml(PlayerInstance activeChar)
	{
		final String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/zone.htm");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(0, 1);
		adminReply.setHtml(htmContent);
		adminReply.replace("%PEACE%", (activeChar.isInsideZone(ZoneId.PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%PVP%", (activeChar.isInsideZone(ZoneId.PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%SIEGE%", (activeChar.isInsideZone(ZoneId.SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%TOWN%", (activeChar.isInsideZone(ZoneId.TOWN) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%CASTLE%", (activeChar.isInsideZone(ZoneId.CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%FORT%", (activeChar.isInsideZone(ZoneId.FORT) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%HQ%", (activeChar.isInsideZone(ZoneId.HQ) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%CLANHALL%", (activeChar.isInsideZone(ZoneId.CLAN_HALL) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%LAND%", (activeChar.isInsideZone(ZoneId.LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%NOLAND%", (activeChar.isInsideZone(ZoneId.NO_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%NOSUMMON%", (activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%WATER%", (activeChar.isInsideZone(ZoneId.WATER) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%SWAMP%", (activeChar.isInsideZone(ZoneId.SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%DANGER%", (activeChar.isInsideZone(ZoneId.DANGER_AREA) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%NOSTORE%", (activeChar.isInsideZone(ZoneId.NO_STORE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		adminReply.replace("%SCRIPT%", (activeChar.isInsideZone(ZoneId.SCRIPT) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
		
		final StringBuilder zones = new StringBuilder(100);
		for (ZoneType zone : ZoneManager.getInstance().getRegion(activeChar).getZones().values())
		{
			if (zone.isCharacterInZone(activeChar))
			{
				if (zone.getName() != null)
				{
					zones.append(zone.getName());
					zones.append("<br1>");
					if (zone.getId() < 300000)
					{
						zones.append("(");
						zones.append(zone.getId());
						zones.append(")");
					}
				}
				else
				{
					zones.append(zone.getId());
				}
				zones.append(" ");
			}
		}
		for (SpawnTerritory territory : ZoneManager.getInstance().getSpawnTerritories(activeChar))
		{
			zones.append(territory.getName());
			zones.append("<br1>");
		}
		adminReply.replace("%ZLIST%", zones.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void getGeoRegionXY(PlayerInstance activeChar)
	{
		int worldX = activeChar.getX();
		int worldY = activeChar.getY();
		int geoX = ((((worldX - (-327680)) >> 4) >> 11) + 10);
		int geoY = ((((worldY - (-262144)) >> 4) >> 11) + 10);
		activeChar.sendMessage("GeoRegion: " + geoX + "_" + geoY + "");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
