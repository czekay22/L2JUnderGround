/*
 * Copyright (C) 2004-2015 L2J United
 * 
 * This file is part of L2J United.
 * 
 * L2J United is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J United is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.plugins;

/**
 * @author UnAfraid
 */
public class ServerPluginInstallFile
{
	private final String _source;
	private String _destination;
	
	private ServerPluginInstallFile(String source)
	{
		_source = source;
	}
	
	public static ServerPluginInstallFile source(String source)
	{
		return new ServerPluginInstallFile(source);
	}
	
	public ServerPluginInstallFile destination(String destination)
	{
		_destination = destination;
		return this;
	}
	
	public String getSource()
	{
		return _source;
	}
	
	public String getDestination()
	{
		return _destination;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": [source=" + _source + ", destination=" + _destination + "]";
	}
}
