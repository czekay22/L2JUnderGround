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

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2DefenderInstance;
import org.l2junity.gameserver.model.actor.instance.L2FortCommanderInstance;
import org.l2junity.gameserver.model.actor.instance.L2SiegeFlagInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.util.Util;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public final class Fear extends AbstractEffect
{
	public static final int FEAR_RANGE = 500;
	
	public Fear(StatsSet params)
	{
	
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		final Creature creature = info.getEffected();
		return creature.isPlayer() || creature.isSummon() || (creature.isAttackable() && //
		!((creature instanceof L2DefenderInstance) || (creature instanceof L2FortCommanderInstance) || //
		(creature instanceof L2SiegeFlagInstance) || (creature.getTemplate().getRace() == Race.SIEGE_WEAPON)));
	}
	
	@Override
	public int getTicks()
	{
		return 5;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		fearAction(null, info.getEffected());
		return false;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		effected.getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		fearAction(effector, effected);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.getEffected().isPlayer())
		{
			info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	private void fearAction(Creature effector, Creature effected)
	{
		double radians = Math.toRadians((effector != null) ? Util.calculateAngleFrom(effector, effected) : Util.convertHeadingToDegree(effected.getHeading()));
		
		int posX = (int) (effected.getX() + (FEAR_RANGE * Math.cos(radians)));
		int posY = (int) (effected.getY() + (FEAR_RANGE * Math.sin(radians)));
		int posZ = effected.getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(effected.getX(), effected.getY(), effected.getZ(), posX, posY, posZ, effected.getInstanceWorld());
		effected.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destination);
	}
}
