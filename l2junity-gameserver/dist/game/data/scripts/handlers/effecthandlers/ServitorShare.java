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

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.EffectFlag;
import org.l2junity.gameserver.model.skills.BuffInfo;

/**
 * Servitor Share effect implementation.<br>
 * Synchronizing effects on player and servitor if one of them gets removed for some reason the same will happen to another. Partner's effect exit is executed in own thread, since there is no more queue to schedule the effects,<br>
 * partner's effect is called while this effect is still exiting issuing an exit call for the effect, causing a stack over flow.
 * @author UnAfraid, Zoey76
 */
public final class ServitorShare extends AbstractEffect
{
	private static final class ScheduledEffectExitTask implements Runnable
	{
		private final Creature _effected;
		private final int _skillId;
		
		public ScheduledEffectExitTask(Creature effected, int skillId)
		{
			_effected = effected;
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			_effected.stopSkillEffects(false, _skillId);
		}
	}
	
	public ServitorShare(StatsSet params)
	{
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final Creature effected = info.getEffected().isSummon() ? ((Summon) info.getEffected()).getOwner() : info.getEffected();
		
		if (effected != null)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new ScheduledEffectExitTask(effected, info.getSkill().getId()), 100);
		}
	}
}
