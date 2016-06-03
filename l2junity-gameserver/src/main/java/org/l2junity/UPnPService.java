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
package org.l2junity;

import java.io.IOException;
import java.net.InetAddress;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author UnAfraid
 */
public class UPnPService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UPnPService.class);
	private static final String PROTOCOL = "TCP";
	
	private final GatewayDiscover _gatewayDiscover = new GatewayDiscover();
	private GatewayDevice _activeGW;
	
	protected UPnPService()
	{
		try
		{
			load();
		}
		catch (Exception e)
		{
			LOGGER.warn("There was an error while initializing:", e);
		}
	}
	
	private void load() throws Exception
	{
		if (!Config.ENABLE_UPNP)
		{
			LOGGER.warn("UPnP Service is disabled.");
			return;
		}
		
		LOGGER.info("Looking for UPnP Gateway Devices...");
		_gatewayDiscover.discover();
		
		// Choose the first active gateway for the tests
		_activeGW = _gatewayDiscover.getValidGateway();
		if (_activeGW == null)
		{
			LOGGER.info("No active UPnP gateway found");
			return;
		}
		
		LOGGER.info("Using UPnP gateway: {}", _activeGW.getFriendlyName());
		LOGGER.info("Using local address: {} External address: {}", _activeGW.getLocalAddress().getHostAddress(), _activeGW.getExternalIPAddress());
		
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			addPortMapping(Config.PORT_GAME, "L2j Game Server");
			
			if (Config.TELNET_ENABLED && Config.TELNET_UPNP_ENABLED)
			{
				addPortMapping(Config.TELNET_PORT, "L2j Telnet Server");
			}
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			addPortMapping(Config.PORT_LOGIN, "L2j Login Server");
		}
	}
	
	public void removeAllPorts() throws Exception
	{
		if (_activeGW != null)
		{
			if (Server.serverMode == Server.MODE_GAMESERVER)
			{
				deletePortMapping(Config.PORT_GAME);
				
				if (Config.TELNET_ENABLED && Config.TELNET_UPNP_ENABLED)
				{
					deletePortMapping(Config.TELNET_PORT);
				}
			}
			else if (Server.serverMode == Server.MODE_LOGINSERVER)
			{
				deletePortMapping(Config.PORT_LOGIN);
			}
		}
	}
	
	private void addPortMapping(int port, String description) throws IOException, SAXException
	{
		final PortMappingEntry portMapping = new PortMappingEntry();
		final InetAddress localAddress = _activeGW.getLocalAddress();
		
		// Attempt to re-map
		if (_activeGW.getSpecificPortMappingEntry(port, PROTOCOL, portMapping))
		{
			_activeGW.deletePortMapping(port, PROTOCOL);
		}
		
		if (_activeGW.addPortMapping(port, port, localAddress.getHostAddress(), PROTOCOL, description))
		{
			LOGGER.info("Mapping successfull on [{}:{}]", localAddress.getHostAddress(), port);
		}
		else
		{
			LOGGER.info("Mapping failed on [{}:{}] - Already mapped?", localAddress.getHostAddress(), port);
		}
	}
	
	private void deletePortMapping(int port) throws IOException, SAXException
	{
		if (_activeGW.deletePortMapping(port, PROTOCOL))
		{
			LOGGER.info("Mapping was deleted from [{}:{}]", _activeGW.getLocalAddress().getHostAddress(), port);
		}
	}
	
	public static UPnPService getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final UPnPService _instance = new UPnPService();
	}
}
