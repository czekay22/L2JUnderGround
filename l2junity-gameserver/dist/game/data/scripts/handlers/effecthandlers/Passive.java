/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * Passive effect implementation.
 * @author Adry_85
 */
public final class Passive extends AbstractEffect
{
	public Passive(StatsSet params)
	{
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return info.getEffected().isAttackable();
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		effected.abortAttack();
		effected.abortCast();
		effected.disableAllSkills();
		effected.setIsImmobilized(true);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().enableAllSkills();
		info.getEffected().setIsImmobilized(false);
	}
}
