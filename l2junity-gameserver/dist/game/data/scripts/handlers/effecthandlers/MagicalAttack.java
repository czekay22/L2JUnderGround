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
import org.l2junity.gameserver.model.actor.Attackable;
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
public final class MagicalAttack extends AbstractEffect
{
	private final double _power;
	private final boolean _overHit;
	private final double _debuffModifier;
	
	public MagicalAttack(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		_overHit = params.getBoolean("overHit", false);
		_debuffModifier = params.getDouble("debuffModifier", 1);
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
		// TODO: Unhardcode Cubic Skill to avoid double damage
		if (effector.isAlikeDead() || (skill.getId() == 4049))
		{
			return;
		}
		
		if (effected.isPlayer() && effected.getActingPlayer().isFakeDeath())
		{
			effected.stopFakeDeath(true);
		}
		
		if (_overHit && effected.isAttackable())
		{
			((Attackable) effected).overhitEnabled(true);
		}
		
		boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		double damage = Formulas.calcMagicDam(effector, effected, skill, effector.getMAtk(), _power, effected.getMDef(), sps, bss, mcrit);
		
		// Apply debuff mod
		if (effected.getEffectList().hasDebuffs())
		{
			damage *= _debuffModifier;
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