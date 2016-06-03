package org.l2junity.gameserver.data.xml.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.l2junity.gameserver.data.xml.IGameXmlReader;
import org.l2junity.gameserver.handler.EffectHandler;
import org.l2junity.gameserver.handler.SkillConditionHandler;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.CommonSkill;
import org.l2junity.gameserver.model.skills.EffectScope;
import org.l2junity.gameserver.model.skills.ISkillCondition;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.SkillConditionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Skill data parser.
 * @author NosBit
 */
public class SkillData implements IGameXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SkillData.class);
	
	private static final Set<String> BLOCK_ITEM_VALUE_ELEMENTS = new HashSet<>();
	private static final Set<String> BLOCK_ITEM_ELEMENTS = new HashSet<>();
	
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private final Map<Integer, Integer> _skillsMaxLevel = new HashMap<>();
	private final Set<Integer> _enchantable = new HashSet<>();
	
	static
	{
		BLOCK_ITEM_VALUE_ELEMENTS.add("item");
		BLOCK_ITEM_VALUE_ELEMENTS.add("value");
		BLOCK_ITEM_ELEMENTS.add("item");
	}
	
	private class NamedParamInfo
	{
		private final String _name;
		private final Integer _fromLevel;
		private final Integer _toLevel;
		private final Integer _fromSubLevel;
		private final Integer _toSubLevel;
		private final Map<Integer, Map<Integer, StatsSet>> _info;
		private final StatsSet _generalInfo;
		
		public NamedParamInfo(String name, Integer fromLevel, Integer toLevel, Integer fromSubLevel, Integer toSubLevel, Map<Integer, Map<Integer, StatsSet>> info, StatsSet generalInfo)
		{
			_name = name;
			_fromLevel = fromLevel;
			_toLevel = toLevel;
			_fromSubLevel = fromSubLevel;
			_toSubLevel = toSubLevel;
			_info = info;
			_generalInfo = generalInfo;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public Integer getFromLevel()
		{
			return _fromLevel;
		}
		
		public Integer getToLevel()
		{
			return _toLevel;
		}
		
		public Integer getFromSubLevel()
		{
			return _fromSubLevel;
		}
		
		public Integer getToSubLevel()
		{
			return _toSubLevel;
		}
		
		public Map<Integer, Map<Integer, StatsSet>> getInfo()
		{
			return _info;
		}
		
		public StatsSet getGeneralInfo()
		{
			return _generalInfo;
		}
	}
	
	protected SkillData()
	{
		load();
	}
	
	/**
	 * Provides the skill hash
	 * @param skill The L2Skill to be hashed
	 * @return getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId * 1021) + skillLevel;
	}
	
	public Skill getSkill(int skillId, int level)
	{
		final Skill result = _skills.get(getSkillHashCode(skillId, level));
		if (result != null)
		{
			return result;
		}
		
		// skill/level not found, fix for transformation scripts
		final int maxLvl = getMaxLevel(skillId);
		// requested level too high
		if ((maxLvl > 0) && (level > maxLvl))
		{
			LOGGER.warn("call to unexisting skill level id: {} requested level: {} max level: {}", skillId, level, maxLvl, new Throwable());
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		}
		
		LOGGER.warn("No skill info found for skill id {} and skill level {}", skillId, level);
		return null;
	}
	
	public int getMaxLevel(int skillId)
	{
		final Integer maxLevel = _skillsMaxLevel.get(skillId);
		return maxLevel != null ? maxLevel : 0;
	}
	
	/**
	 * Verifies if the given skill ID correspond to an enchantable skill.
	 * @param skillId the skill ID
	 * @return {@code true} if the skill is enchantable, {@code false} otherwise
	 */
	public boolean isEnchantable(int skillId)
	{
		return _enchantable.contains(skillId);
	}
	
	/**
	 * @param addNoble
	 * @param hasCastle
	 * @return an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public List<Skill> getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		final List<Skill> temp = new LinkedList<>();
		
		temp.add(_skills.get(SkillData.getSkillHashCode(CommonSkill.IMPRIT_OF_LIGHT.getId(), 1)));
		temp.add(_skills.get(SkillData.getSkillHashCode(CommonSkill.IMPRIT_OF_DARKNESS.getId(), 1)));
		
		temp.add(_skills.get(SkillData.getSkillHashCode(247, 1))); // Build Headquarters
		
		if (addNoble)
		{
			temp.add(_skills.get(SkillData.getSkillHashCode(326, 1))); // Build Advanced Headquarters
		}
		if (hasCastle)
		{
			temp.add(_skills.get(SkillData.getSkillHashCode(844, 1))); // Outpost Construction
			temp.add(_skills.get(SkillData.getSkillHashCode(845, 1))); // Outpost Demolition
		}
		return temp;
	}
	
	@Override
	public boolean isValidating()
	{
		return false;
	}
	
	@Override
	public synchronized void load()
	{
		_skills.clear();
		_skillsMaxLevel.clear();
		_enchantable.clear();
		parseDatapackDirectory("data/stats/skills/", true);
		LOGGER.info("Loaded {} Skills.", _skills.size());
	}
	
	public void reload()
	{
		load();
		// Reload Skill Tree as well.
		SkillTreesData.getInstance().load();
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node listNode = node.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
				{
					if ("skill".equalsIgnoreCase(listNode.getNodeName()))
					{
						NamedNodeMap attributes = listNode.getAttributes();
						final Map<Integer, Set<Integer>> levels = new HashMap<>();
						final Map<Integer, Map<Integer, StatsSet>> skillInfo = new HashMap<>();
						final StatsSet generalSkillInfo = new StatsSet();
						
						parseAttributes(attributes, "", generalSkillInfo);
						
						final Map<String, Map<Integer, Map<Integer, Object>>> variableValues = new HashMap<>();
						final Map<String, Object> variableGeneralValues = new HashMap<>();
						final Map<EffectScope, List<NamedParamInfo>> effectParamInfo = new HashMap<>();
						final Map<SkillConditionScope, List<NamedParamInfo>> conditionParamInfo = new HashMap<>();
						for (Node skillNode = listNode.getFirstChild(); skillNode != null; skillNode = skillNode.getNextSibling())
						{
							final String skillNodeName = skillNode.getNodeName();
							switch (skillNodeName.toLowerCase())
							{
								case "variable":
								{
									attributes = skillNode.getAttributes();
									final String name = "@" + parseString(attributes, "name");
									variableGeneralValues.put(name, parseValues(skillNode, variableValues.computeIfAbsent(name, k -> new HashMap<>())));
									break;
								}
								case "#text":
								{
									break;
								}
								default:
								{
									final EffectScope effectScope = EffectScope.findByXmlNodeName(skillNodeName);
									if (effectScope != null)
									{
										for (Node effectsNode = skillNode.getFirstChild(); effectsNode != null; effectsNode = effectsNode.getNextSibling())
										{
											switch (effectsNode.getNodeName().toLowerCase())
											{
												case "effect":
												{
													effectParamInfo.computeIfAbsent(effectScope, k -> new LinkedList<>()).add(parseNamedParamInfo(effectsNode, variableValues, variableGeneralValues));
													break;
												}
											}
										}
										break;
									}
									final SkillConditionScope skillConditionScope = SkillConditionScope.findByXmlNodeName(skillNodeName);
									if (skillConditionScope != null)
									{
										for (Node conditionNode = skillNode.getFirstChild(); conditionNode != null; conditionNode = conditionNode.getNextSibling())
										{
											switch (conditionNode.getNodeName().toLowerCase())
											{
												case "condition":
												{
													conditionParamInfo.computeIfAbsent(skillConditionScope, k -> new LinkedList<>()).add(parseNamedParamInfo(conditionNode, variableValues, variableGeneralValues));
													break;
												}
											}
										}
									}
									else
									{
										parseInfo(skillNode, variableValues, variableGeneralValues, skillInfo, generalSkillInfo);
									}
									break;
								}
							}
						}
						
						final int fromLevel = generalSkillInfo.getInt(".fromLevel", 1);
						final int toLevel = generalSkillInfo.getInt(".toLevel", 0);
						
						for (int i = fromLevel; i <= toLevel; i++)
						{
							levels.computeIfAbsent(i, k -> new HashSet<>()).add(0);
						}
						
						skillInfo.forEach((level, subLevelMap) ->
						{
							subLevelMap.forEach((subLevel, statsSet) ->
							{
								levels.computeIfAbsent(level, k -> new HashSet<>()).add(subLevel);
							});
						});
						
						Stream.concat(effectParamInfo.values().stream(), conditionParamInfo.values().stream()).forEach(namedParamInfos ->
						{
							namedParamInfos.forEach(namedParamInfo ->
							{
								namedParamInfo.getInfo().forEach((level, subLevelMap) ->
								{
									subLevelMap.forEach((subLevel, statsSet) ->
									{
										levels.computeIfAbsent(level, k -> new HashSet<>()).add(subLevel);
									});
								});
								
								if ((namedParamInfo.getFromLevel() != null) && (namedParamInfo.getToLevel() != null))
								{
									for (int i = namedParamInfo.getFromLevel(); i <= namedParamInfo.getToLevel(); i++)
									{
										if ((namedParamInfo.getFromSubLevel() != null) && (namedParamInfo.getToSubLevel() != null))
										{
											for (int j = namedParamInfo.getFromSubLevel(); j <= namedParamInfo.getToSubLevel(); j++)
											{
												
												levels.computeIfAbsent(i, k -> new HashSet<>()).add(j);
											}
										}
										else
										{
											levels.computeIfAbsent(i, k -> new HashSet<>()).add(0);
										}
									}
								}
							});
						});
						
						levels.forEach((level, subLevels) ->
						{
							subLevels.forEach(subLevel ->
							{
								final StatsSet statsSet = Optional.ofNullable(skillInfo.getOrDefault(level, Collections.emptyMap()).get(subLevel)).orElseGet(() -> new StatsSet());
								generalSkillInfo.getSet().forEach((k, v) -> statsSet.getSet().putIfAbsent(k, v));
								statsSet.set(".level", level);
								statsSet.set(".subLevel", subLevel);
								final Skill skill = new Skill(statsSet);
								forEachNamedParamInfoParam(effectParamInfo, level, subLevel, ((effectScope, params) ->
								{
									final String effectName = params.getString(".name");
									params.remove(".name");
									try
									{
										final Function<StatsSet, AbstractEffect> effectFunction = EffectHandler.getInstance().getHandlerFactory(effectName);
										if (effectFunction != null)
										{
											skill.addEffect(effectScope, effectFunction.apply(params));
										}
										else
										{
											LOGGER.warn("Missing effect for Skill Id[{}] Level[{}] SubLevel[{}] Effect Scope[{}] Effect Name[{}]", statsSet.getInt(".id"), level, subLevel, effectScope, effectName);
										}
									}
									catch (Exception e)
									{
										LOGGER.warn("Failed loading effect for Skill Id[{}] Level[{}] SubLevel[{}] Effect Scope[{}] Effect Name[{}]", statsSet.getInt(".id"), level, subLevel, effectScope, effectName, e);
									}
								}));
								
								forEachNamedParamInfoParam(conditionParamInfo, level, subLevel, ((skillConditionScope, params) ->
								{
									final String conditionName = params.getString(".name");
									params.remove(".name");
									try
									{
										final Function<StatsSet, ISkillCondition> conditionFunction = SkillConditionHandler.getInstance().getHandlerFactory(conditionName);
										if (conditionFunction != null)
										{
											skill.addCondition(skillConditionScope, conditionFunction.apply(params));
										}
										else
										{
											LOGGER.warn("Missing condition for Skill Id[{}] Level[{}] SubLevel[{}] Effect Scope[{}] Effect Name[{}]", statsSet.getInt(".id"), level, subLevel, skillConditionScope, conditionName);
										}
									}
									catch (Exception e)
									{
										LOGGER.warn("Failed loading condition for Skill Id[{}] Level[{}] SubLevel[{}] Condition Scope[{}] Condition Name[{}]", statsSet.getInt(".id"), level, subLevel, skillConditionScope, conditionName, e);
									}
								}));
								
								_skills.put(getSkillHashCode(skill), skill);
								_skillsMaxLevel.merge(skill.getId(), skill.getLevel(), Integer::max);
								// TODO: add enchantable
							});
						});
					}
				}
			}
		}
	}
	
	private <T> void forEachNamedParamInfoParam(Map<T, List<NamedParamInfo>> paramInfo, int level, int subLevel, BiConsumer<T, StatsSet> consumer)
	{
		paramInfo.forEach((scope, namedParamInfos) ->
		{
			namedParamInfos.forEach(namedParamInfo ->
			{
				if (((namedParamInfo.getFromLevel() == null) && (namedParamInfo.getToLevel() == null)) || ((namedParamInfo.getFromLevel() <= level) && (namedParamInfo.getToLevel() >= level)))
				{
					if (((namedParamInfo.getFromSubLevel() == null) && (namedParamInfo.getToSubLevel() == null)) || ((namedParamInfo.getFromSubLevel() <= subLevel) && (namedParamInfo.getToSubLevel() >= subLevel)))
					{
						final StatsSet params = Optional.ofNullable(namedParamInfo.getInfo().getOrDefault(level, Collections.emptyMap()).get(subLevel)).orElseGet(() -> new StatsSet());
						namedParamInfo.getGeneralInfo().getSet().forEach((k, v) -> params.getSet().putIfAbsent(k, v));
						params.set(".name", namedParamInfo.getName());
						consumer.accept(scope, params);
					}
				}
			});
		});
	}
	
	private NamedParamInfo parseNamedParamInfo(Node node, Map<String, Map<Integer, Map<Integer, Object>>> variableValues, Map<String, Object> variableGeneralValues)
	{
		final NamedNodeMap attributes = node.getAttributes();
		final String name = parseString(attributes, "name");
		final Integer level = parseInteger(attributes, "level");
		final Integer fromLevel = parseInteger(attributes, "fromLevel", level);
		final Integer toLevel = parseInteger(attributes, "toLevel", level);
		final Integer subLevel = parseInteger(attributes, "subLevel", 0);
		final Integer fromSubLevel = parseInteger(attributes, "fromSubLevel", subLevel);
		final Integer toSubLevel = parseInteger(attributes, "toSubLevel", subLevel);
		final Map<Integer, Map<Integer, StatsSet>> info = new HashMap<>();
		final StatsSet generalInfo = new StatsSet();
		for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (!node.getNodeName().equals("#text"))
			{
				parseInfo(node, variableValues, variableGeneralValues, info, generalInfo);
			}
		}
		return new NamedParamInfo(name, fromLevel, toLevel, fromSubLevel, toSubLevel, info, generalInfo);
	}
	
	private void parseInfo(Node node, Map<String, Map<Integer, Map<Integer, Object>>> variableValues, Map<String, Object> variableGeneralValues, Map<Integer, Map<Integer, StatsSet>> info, StatsSet generalInfo)
	{
		Map<Integer, Map<Integer, Object>> values = new HashMap<>();
		Object generalValue = parseValues(node, values);
		if (generalValue != null)
		{
			String stringGeneralValue = String.valueOf(generalValue);
			if (stringGeneralValue.startsWith("@"))
			{
				Map<Integer, Map<Integer, Object>> tableValue = variableValues.get(stringGeneralValue);
				if (tableValue != null)
				{
					if (!tableValue.isEmpty())
					{
						values = tableValue;
					}
					else
					{
						generalInfo.set(node.getNodeName(), variableGeneralValues.get(stringGeneralValue));
					}
				}
				else
				{
					throw new IllegalArgumentException("undefined variable " + stringGeneralValue);
				}
			}
			else
			{
				generalInfo.set(node.getNodeName(), generalValue);
			}
		}
		
		values.forEach((level, subLevelMap) ->
		{
			subLevelMap.forEach((subLevel, value) ->
			{
				info.computeIfAbsent(level, k -> new HashMap<>()).computeIfAbsent(subLevel, k -> new StatsSet()).set(node.getNodeName(), value);
			});
		});
	}
	
	private Object parseValues(Node node, Map<Integer, Map<Integer, Object>> values)
	{
		Object parsedValue = parseValue(node, true, false, Collections.emptyMap());
		if (parsedValue != null)
		{
			return parsedValue;
		}
		
		List<Object> list = null;
		for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (node.getNodeName().equalsIgnoreCase("value"))
			{
				final NamedNodeMap attributes = node.getAttributes();
				final Integer level = parseInteger(attributes, "level");
				if (level != null)
				{
					parsedValue = parseValue(node, false, false, Collections.emptyMap());
					if (parsedValue != null)
					{
						final Integer subLevel = parseInteger(attributes, "subLevel", 0);
						values.computeIfAbsent(level, k -> new HashMap<>()).put(subLevel, parsedValue);
					}
				}
				else
				{
					final int fromLevel = parseInteger(attributes, "fromLevel");
					final int toLevel = parseInteger(attributes, "toLevel");
					final int fromSubLevel = parseInteger(attributes, "fromSubLevel", 0);
					final int toSubLevel = parseInteger(attributes, "toSubLevel", 0);
					for (int i = fromLevel; i <= toLevel; i++)
					{
						for (int j = fromSubLevel; j <= toSubLevel; j++)
						{
							Map<Integer, Object> subValues = values.computeIfAbsent(i, k -> new HashMap<>());
							Map<String, Double> variables = new HashMap<>();
							variables.put("index", (i - fromLevel) + 1d);
							variables.put("subIndex", (j - fromSubLevel) + 1d);
							Object base = values.getOrDefault(i, Collections.emptyMap()).get(0);
							if ((base != null) && !(base instanceof StatsSet))
							{
								variables.put("base", Double.parseDouble(String.valueOf(base)));
							}
							parsedValue = parseValue(node, false, false, variables);
							if (parsedValue != null)
							{
								subValues.put(j, parsedValue);
							}
						}
					}
				}
			}
		}
		return list;
	}
	
	Object parseValue(Node node, boolean blockValue, boolean parseAttributes, Map<String, Double> variables)
	{
		StatsSet statsSet = null;
		List<Object> list = null;
		Object text = null;
		if (parseAttributes && (!node.getNodeName().equals("value") || !blockValue) && (node.getAttributes().getLength() > 0))
		{
			statsSet = new StatsSet();
			parseAttributes(node.getAttributes(), "", statsSet, variables);
		}
		for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
		{
			final String nodeName = node.getNodeName();
			switch (node.getNodeName())
			{
				case "#text":
				{
					final String value = node.getNodeValue().trim();
					if (!value.isEmpty())
					{
						text = parseNodeValue(value, variables);
					}
					break;
				}
				case "item":
				{
					if (list == null)
					{
						list = new LinkedList<>();
					}
					
					final Object value = parseValue(node, false, true, variables);
					if (value != null)
					{
						list.add(value);
					}
					break;
				}
				case "value":
				{
					if (blockValue)
					{
						break;
					}
				}
				default:
				{
					final Object value = parseValue(node, false, true, variables);
					if (value != null)
					{
						if (statsSet == null)
						{
							statsSet = new StatsSet();
						}
						
						statsSet.set(nodeName, value);
					}
				}
			}
		}
		if (list != null)
		{
			if (text != null)
			{
				throw new IllegalArgumentException("Text and list in same node are not allowed. Node[" + node + "]");
			}
			if (statsSet != null)
			{
				statsSet.set(".", list);
			}
			else
			{
				return list;
			}
		}
		if (text != null)
		{
			if (list != null)
			{
				throw new IllegalArgumentException("Text and list in same node are not allowed. Node[" + node + "]");
			}
			if (statsSet != null)
			{
				statsSet.set(".", text);
			}
			else
			{
				return text;
			}
		}
		return statsSet;
	}
	
	private void parseAttributes(NamedNodeMap attributes, String prefix, StatsSet statsSet, Map<String, Double> variables)
	{
		for (int i = 0; i < attributes.getLength(); i++)
		{
			final Node attributeNode = attributes.item(i);
			statsSet.set(prefix + "." + attributeNode.getNodeName(), parseNodeValue(attributeNode.getNodeValue(), variables));
		}
	}
	
	private void parseAttributes(NamedNodeMap attributes, String prefix, StatsSet statsSet)
	{
		parseAttributes(attributes, prefix, statsSet, Collections.emptyMap());
	}
	
	private Object parseNodeValue(String value, Map<String, Double> variables)
	{
		if (value.startsWith("{") && value.endsWith("}"))
		{
			return new ExpressionBuilder(value).variables(variables.keySet()).build().setVariables(variables).evaluate();
		}
		return value;
	}
	
	public static SkillData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillData _instance = new SkillData();
	}
}
