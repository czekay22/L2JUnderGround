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
package org.l2junity.gameserver.model.cubic;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.cubic.conditions.ICubicCondition;
import org.l2junity.gameserver.model.holders.SkillHolder;

/**
 * @author UnAfraid
 */
public class CubicSkill extends SkillHolder implements ICubicConditionHolder
{
	private final int _triggerRate;
	private final int _successRate;
	private final boolean _canUseOnStaticObjects;
	private final CubicSkillTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	private final boolean _targetDebuff;
	
	public CubicSkill(StatsSet set)
	{
		super(set.getInt("id"), set.getInt("level"));
		_triggerRate = set.getInt("triggerRate", 100);
		_successRate = set.getInt("successRate", 100);
		_canUseOnStaticObjects = set.getBoolean("canUseOnStaticObjects", false);
		_targetType = set.getEnum("target", CubicSkillTargetType.class, CubicSkillTargetType.TARGET);
		_targetDebuff = set.getBoolean("targetDebuff", false);
	}
	
	public int getTriggerRate()
	{
		return _triggerRate;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public boolean canUseOnStaticObjects()
	{
		return _canUseOnStaticObjects;
	}
	
	public CubicSkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public boolean isTargetingDebuff()
	{
		return _targetDebuff;
	}
	
	@Override
	public boolean validateConditions(CubicInstance cubic, Creature owner, Creature target)
	{
		return (!_targetDebuff || (_targetDebuff && target.getEffectList().hasDebuffs())) && (_conditions.isEmpty() || _conditions.stream().allMatch(condition -> condition.test(cubic, owner, target)));
	}
	
	@Override
	public void addCondition(ICubicCondition condition)
	{
		_conditions.add(condition);
	}
	
	@Override
	public String toString()
	{
		return "Cubic skill id: " + getSkillId() + " level: " + getSkillLvl() + " triggerRate: " + _triggerRate + " successRate: " + _successRate + " canUseOnStaticObjects: " + _canUseOnStaticObjects + " targetType: " + _targetType + " isTargetingDebuff: " + _targetDebuff + System.lineSeparator();
	}
}
