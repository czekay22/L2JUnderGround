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

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureSkillUse;
import org.l2junity.gameserver.model.events.listeners.FunctionEventListener;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.skills.BuffInfo;

/**
 * Block Skills by isMagic type.
 * @author Nik
 */
public final class BlockSkill extends AbstractEffect
{
	private final int[] _magicTypes;
	
	public BlockSkill(StatsSet params)
	{
		_magicTypes = params.getIntArray("magicTypes", ";");
	}
	
	public TerminateReturn onSkillUseEvent(OnCreatureSkillUse event)
	{
		if (CommonUtil.contains(_magicTypes, event.getSkill().getMagicType()))
		{
			return new TerminateReturn(true, true, true);
		}
		
		return null;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if ((_magicTypes == null) || (_magicTypes.length == 0))
		{
			return;
		}
		
		info.getEffected().addListener(new FunctionEventListener(info.getEffected(), EventType.ON_CREATURE_SKILL_USE, (OnCreatureSkillUse event) -> onSkillUseEvent(event), this));
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().removeListenerIf(EventType.ON_CREATURE_SKILL_USE, listener -> listener.getOwner() == this);
	}
	
}
