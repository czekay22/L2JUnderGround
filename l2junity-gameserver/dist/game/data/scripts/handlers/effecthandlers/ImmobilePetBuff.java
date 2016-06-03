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
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.BuffInfo;

/**
 * Immobile Pet Buff effect implementation.
 * @author demonia
 */
public final class ImmobilePetBuff extends AbstractEffect
{
	public ImmobilePetBuff(StatsSet params)
	{
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().setIsImmobilized(false);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isSummon() && info.getEffector().isPlayer() && (((Summon) info.getEffected()).getOwner() == info.getEffector()))
		{
			info.getEffected().setIsImmobilized(true);
		}
	}
}