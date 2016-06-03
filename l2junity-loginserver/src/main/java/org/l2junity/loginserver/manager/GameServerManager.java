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
package org.l2junity.loginserver.manager;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.l2junity.commons.util.IXmlReader;
import org.l2junity.loginserver.model.GameServer;
import org.l2junity.loginserver.model.enums.AgeLimit;
import org.l2junity.loginserver.model.enums.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author NosBit
 */
public class GameServerManager implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GameServerManager.class);

	private final Map<Short, GameServer> _gameServers = new HashMap<>();

	protected GameServerManager()
	{
		load();
	}

	@Override
	public void load()
	{
		parseFile(new File("config/GameServers.xml"));
		LOGGER.info("Loaded {} game servers.", _gameServers.size());
	}

	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node gameServersNode = doc.getFirstChild(); gameServersNode != null; gameServersNode = gameServersNode.getNextSibling())
		{
			if (gameServersNode.getNodeName().equals("gameServers"))
			{
				for (Node gameServerNode = gameServersNode.getFirstChild(); gameServerNode != null; gameServerNode = gameServerNode.getNextSibling())
				{
					if (gameServerNode.getNodeName().equals("gameServer"))
					{
						final NamedNodeMap attributes = gameServerNode.getAttributes();
						final short id = parseShort(attributes, "id");
						final String name = parseString(attributes, "name", "");
						final boolean showing = parseBoolean(attributes, "showing", false);
						final AgeLimit ageLimit = parseEnum(attributes, AgeLimit.class, "ageLimit", AgeLimit.NONE);

						Set<ServerType> serverTypes = null;
						for (Node serverTypesNode = gameServerNode.getFirstChild(); serverTypesNode != null; serverTypesNode = serverTypesNode.getNextSibling())
						{
							if (serverTypesNode.getNodeName().equals("serverTypes"))
							{
								for (Node serverTypeNode = serverTypesNode.getFirstChild(); serverTypeNode != null; serverTypeNode = serverTypeNode.getNextSibling())
								{
									if (serverTypeNode.getNodeName().equals("serverType"))
									{
										if(serverTypes == null)
										{
											serverTypes = new HashSet<>();
										}
										serverTypes.add(parseEnum(serverTypeNode.getFirstChild(), ServerType.class));
									}
								}
							}
						}
						_gameServers.put(id, new GameServer(id, name, showing, ageLimit, serverTypes != null ? serverTypes : Collections.emptySet()));
					}
				}
			}
		}
	}

	public static GameServerManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final GameServerManager _instance = new GameServerManager();
	}
}
