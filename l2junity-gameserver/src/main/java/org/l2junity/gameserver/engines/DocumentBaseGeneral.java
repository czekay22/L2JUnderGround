/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.engines;

import java.io.File;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.conditions.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A dummy class designed only to parse conditions
 * @author UnAfraid
 */
public class DocumentBaseGeneral extends DocumentBase
{
	protected DocumentBaseGeneral(File file)
	{
		super(file);
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return null;
	}
	
	@Override
	protected String getTableValue(String name)
	{
		return null;
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		return null;
	}
	
	public Condition parseCondition(Node n)
	{
		return super.parseCondition(n, null);
	}
	
	public static DocumentBaseGeneral getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DocumentBaseGeneral _instance = new DocumentBaseGeneral(null);
	}
}
