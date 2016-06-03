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
package org.l2junity.gameserver.script.faenor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.script.ScriptContext;

import org.l2junity.Config;
import org.l2junity.commons.util.file.filter.XMLFilter;
import org.l2junity.gameserver.script.Parser;
import org.l2junity.gameserver.script.ParserNotCreatedException;
import org.l2junity.gameserver.script.ScriptDocument;
import org.l2junity.gameserver.script.ScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FaenorScriptEngine.class);
	public static final String PACKAGE_DIRECTORY = "data/faenor/";
	
	protected FaenorScriptEngine()
	{
		final File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);
		final File[] files = packDirectory.listFiles(new XMLFilter());
		for (File file : files)
		{
			try (InputStream in = new FileInputStream(file))
			{
				parseScript(new ScriptDocument(file.getName(), in), null);
			}
			catch (IOException e)
			{
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}
	
	public void parseScript(ScriptDocument script, ScriptContext context)
	{
		Node node = script.getDocument().getFirstChild();
		String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		
		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (ParserNotCreatedException e)
		{
			LOGGER.warn("ERROR: No parser registered for Script: {}: ", parserClass, e);
		}
		
		if (parser == null)
		{
			LOGGER.warn("Unknown Script Type: {}", script.getName());
			return;
		}
		
		try
		{
			parser.parseScript(node, context);
			LOGGER.info("Loaded  {} successfully.", script.getName());
		}
		catch (Exception e)
		{
			LOGGER.warn("Script Parsing Failed: ", e);
		}
	}
	
	public static FaenorScriptEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorScriptEngine _instance = new FaenorScriptEngine();
	}
}
