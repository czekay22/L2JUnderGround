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
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.ExRegenMax;

/**
 * Heal Over Time effect implementation.
 */
public final class HealOverTime extends AbstractEffect
{
	private final double _power;
	
	public HealOverTime(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead() || info.getEffected().isDoor())
		{
			return false;
		}
		
		double hp = info.getEffected().getCurrentHp();
		double maxhp = info.getEffected().getMaxRecoverableHp();
		
		// Not needed to set the HP and send update packet if player is already at max HP
		if (hp >= maxhp)
		{
			return false;
		}
		
		hp += _power * getTicksMultiplier();
		hp = Math.min(hp, maxhp);
		info.getEffected().setCurrentHp(hp, false);
		info.getEffected().broadcastStatusUpdate(info.getEffector());
		return info.getSkill().isToggle();
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Creature effected = info.getEffected();
		final Skill skill = info.getSkill();
		if (effected.isPlayer() && (getTicks() > 0) && (skill.getAbnormalType() == AbnormalType.HP_RECOVER))
		{
			effected.sendPacket(new ExRegenMax(info.getAbnormalTime(), getTicks(), _power));
		}
	}
}
