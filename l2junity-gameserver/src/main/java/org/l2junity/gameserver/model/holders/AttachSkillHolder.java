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
package org.l2junity.gameserver.model.holders;

import org.l2junity.gameserver.model.StatsSet;

/**
 * @author Nik
 */
public class AttachSkillHolder extends SkillHolder
{
	private final int _requiredSkillId;
	private final int _requiredSkillLevel;
	
	public AttachSkillHolder(int skillId, int skillLevel, int requiredSkillId, int requiredSkillLevel)
	{
		super(skillId, skillLevel);
		_requiredSkillId = requiredSkillId;
		_requiredSkillLevel = requiredSkillLevel;
	}
	
	public int getRequiredSkillId()
	{
		return _requiredSkillId;
	}
	
	public int getRequiredSkillLevel()
	{
		return _requiredSkillLevel;
	}
	
	public static AttachSkillHolder fromStatsSet(StatsSet set)
	{
		return new AttachSkillHolder(set.getInt("skillId"), set.getInt("skillLevel", 1), set.getInt("requiredSkillId"), set.getInt("requiredSkillLevel", 1));
	}
}
