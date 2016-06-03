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

import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Formulas;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * Soul Blow effect implementation.
 * @author Adry_85
 */
public final class SoulBlow extends AbstractEffect
{
	private final double _power;
	private final double _chance;
	private final boolean _overHit;
	
	public SoulBlow(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		_chance = params.getDouble("chance", 0);
		_overHit = params.getBoolean("overHit", false);
	}
	
	/**
	 * If is not evaded and blow lands.
	 * @param effector
	 * @param effected
	 * @param skill
	 */
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return !Formulas.calcPhysicalSkillEvasion(effector, effected, skill) && Formulas.calcBlowSuccess(effector, effected, skill, _chance);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHYSICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (effector.isAlikeDead())
		{
			return;
		}
		
		if (_overHit && effected.isAttackable())
		{
			((Attackable) effected).overhitEnabled(true);
		}
		
		boolean ss = skill.useSoulShot() && effector.isChargedShot(ShotType.SOULSHOTS);
		byte shld = Formulas.calcShldUse(effector, effected);
		double damage = Formulas.calcBlowDamage(effector, effected, skill, false, _power, shld, ss);
		if ((skill.getMaxSoulConsumeCount() > 0) && effector.isPlayer())
		{
			// Souls Formula (each soul increase +4%)
			int chargedSouls = (effector.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()) ? effector.getActingPlayer().getChargedSouls() : skill.getMaxSoulConsumeCount();
			damage *= 1 + (chargedSouls * 0.04);
		}
		
		// Check if damage should be reflected
		Formulas.calcDamageReflected(effector, effected, skill, true);
		
		final double damageCap = effected.getStat().getValue(Stats.DAMAGE_LIMIT);
		if (damageCap > 0)
		{
			damage = Math.min(damage, damageCap);
		}
		
		effected.reduceCurrentHp(damage, effector, skill, false, false, true, false);
		
		// Manage attack or cast break of the target (calculating rate, sending message...)
		if (!effected.isRaid() && Formulas.calcAtkBreak(effected, damage))
		{
			effected.breakAttack();
			effected.breakCast();
		}
		
		if (effector.isPlayer())
		{
			PlayerInstance activePlayer = effector.getActingPlayer();
			activePlayer.sendDamageMessage(effected, skill, (int) damage, true, false);
		}
	}
}