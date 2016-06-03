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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * Block Buff Slot effect implementation.
 * @author Zoey76
 */
public final class BlockAbnormalSlot extends AbstractEffect
{
	private final Set<AbnormalType> _blockAbnormalSlots;
	
	public BlockAbnormalSlot(StatsSet params)
	{
		String blockAbnormalSlots = params.getString("slot", null);
		if ((blockAbnormalSlots != null) && !blockAbnormalSlots.isEmpty())
		{
			_blockAbnormalSlots = new HashSet<>();
			for (String slot : blockAbnormalSlots.split(";"))
			{
				_blockAbnormalSlots.add(AbnormalType.getAbnormalType(slot));
			}
		}
		else
		{
			_blockAbnormalSlots = Collections.<AbnormalType> emptySet();
		}
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		effected.getEffectList().addBlockedAbnormalTypes(_blockAbnormalSlots);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getEffectList().removeBlockedAbnormalTypes(_blockAbnormalSlots);
	}
}
