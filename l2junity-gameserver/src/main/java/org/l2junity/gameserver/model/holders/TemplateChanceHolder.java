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

/**
 * An object for holding template id and chance
 * @author Nik
 */
public class TemplateChanceHolder
{
	private final int _templateId;
	private final int _minChance;
	private final int _maxChance;
	
	public TemplateChanceHolder(int templateId, int minChance, int maxChance)
	{
		_templateId = templateId;
		_minChance = minChance;
		_maxChance = maxChance;
	}
	
	public final int getTemplateId()
	{
		return _templateId;
	}
	
	public final boolean calcChance(int chance)
	{
		return (_maxChance > chance) && (chance >= _minChance);
	}
	
	@Override
	public String toString()
	{
		return "[TemplateId: " + _templateId + " minChance: " + _minChance + " maxChance: " + _minChance + "]";
	}
}