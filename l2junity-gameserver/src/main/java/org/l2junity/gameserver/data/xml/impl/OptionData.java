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
package org.l2junity.gameserver.data.xml.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.l2junity.commons.util.IXmlReader;
import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.handler.EffectHandler;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.options.Options;
import org.l2junity.gameserver.model.options.OptionsSkillHolder;
import org.l2junity.gameserver.model.options.OptionsSkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author UnAfraid
 */
public class OptionData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OptionData.class);
	
	private final Map<Integer, Options> _optionData = new HashMap<>();
	
	protected OptionData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_optionData.clear();
		parseDatapackDirectory("data/stats/options", false);
		LOGGER.info("Loaded: {} Options.", _optionData.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "option", optionNode ->
		{
			final int id = parseInteger(optionNode.getAttributes(), "id");
			final Options option = new Options(id);
			
			forEach(optionNode, IXmlReader::isNode, innerNode ->
			{
				switch (innerNode.getNodeName())
				{
					case "effects":
					{
						forEach(innerNode, "effect", effectNode ->
						{
							final String name = parseString(effectNode.getAttributes(), "name");
							final StatsSet params = new StatsSet();
							forEach(effectNode, IXmlReader::isNode, paramNode ->
							{
								params.set(paramNode.getNodeName(), SkillData.getInstance().parseValue(paramNode, true, false, Collections.emptyMap()));
							});
							option.addEffect(EffectHandler.getInstance().getHandlerFactory(name).apply(params));
						});
						break;
					}
					case "active_skill":
					{
						option.setActiveSkill(new SkillHolder(parseInteger(innerNode.getAttributes(), "id"), parseInteger(innerNode.getAttributes(), "level")));
						break;
					}
					case "passive_skill":
					{
						option.setPassiveSkill(new SkillHolder(parseInteger(innerNode.getAttributes(), "id"), parseInteger(innerNode.getAttributes(), "level")));
						break;
					}
					case "attack_skill":
					{
						option.addActivationSkill(new OptionsSkillHolder(parseInteger(innerNode.getAttributes(), "id"), parseInteger(innerNode.getAttributes(), "level"), parseDouble(innerNode.getAttributes(), "chance"), OptionsSkillType.ATTACK));
						break;
					}
					case "magic_skill":
					{
						option.addActivationSkill(new OptionsSkillHolder(parseInteger(innerNode.getAttributes(), "id"), parseInteger(innerNode.getAttributes(), "level"), parseDouble(innerNode.getAttributes(), "chance"), OptionsSkillType.MAGIC));
						break;
					}
					case "critical_skill":
					{
						option.addActivationSkill(new OptionsSkillHolder(parseInteger(innerNode.getAttributes(), "id"), parseInteger(innerNode.getAttributes(), "level"), parseDouble(innerNode.getAttributes(), "chance"), OptionsSkillType.CRITICAL));
						break;
					}
				}
			});
			_optionData.put(option.getId(), option);
		}));
	}
	
	public Options getOptions(int id)
	{
		return _optionData.get(id);
	}
	
	/**
	 * Gets the single instance of OptionsData.
	 * @return single instance of OptionsData
	 */
	public static final OptionData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OptionData _instance = new OptionData();
	}
}
