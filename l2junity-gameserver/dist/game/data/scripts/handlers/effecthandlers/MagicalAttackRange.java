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

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.stats.Formulas;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * Magical Attack effect implementation.
 * @author Adry_85
 */
public final class MagicalAttackRange extends AbstractEffect
{
	private final double _power;
	private final double _shieldDefPercent;
	
	public MagicalAttackRange(StatsSet params)
	{
		_power = params.getDouble("power");
		_shieldDefPercent = params.getDouble("shieldDefPercent", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MAGICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (effected.isPlayer() && effected.getActingPlayer().isFakeDeath())
		{
			effected.stopFakeDeath(true);
		}
		
		double mDef = effected.getMDef();
		switch (Formulas.calcShldUse(effector, effected))
		{
			case Formulas.SHIELD_DEFENSE_SUCCEED:
			{
				mDef += ((effected.getShldDef() * _shieldDefPercent) / 100);
				break;
			}
			case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
			{
				mDef = -1;
				break;
			}
		}
		
		double damage = 1;
		final boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		
		if (mDef != -1)
		{
			boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
			boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
			
			damage = Formulas.calcMagicDam(effector, effected, skill, effector.getMAtk(), _power, mDef, sps, bss, mcrit);
		}
		
		// Manage attack or cast break of the target (calculating rate, sending message...)
		if (!effected.isRaid() && Formulas.calcAtkBreak(effected, damage))
		{
			effected.breakAttack();
			effected.breakCast();
		}
		
		// Shield Deflect Magic: Reflect all damage on caster.
		if (effected.getStat().getValue(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0) > Rnd.get(100))
		{
			effector.reduceCurrentHp(damage, effected, skill, false, false, mcrit, true);
		}
		else
		{
			effected.reduceCurrentHp(damage, effector, skill, false, false, mcrit, false);
			effector.sendDamageMessage(effected, skill, (int) damage, mcrit, false);
		}
	}
}