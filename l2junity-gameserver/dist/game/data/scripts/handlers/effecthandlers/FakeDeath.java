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
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.EffectFlag;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.network.client.send.ChangeWaitType;
import org.l2junity.gameserver.network.client.send.Revive;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Fake Death effect implementation.
 * @author mkizub
 */
public final class FakeDeath extends AbstractEffect
{
	private final double _power;
	
	public FakeDeath(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.FAKE_DEATH.getMask();
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		final double manaDam = _power * getTicksMultiplier();
		if (manaDam > info.getEffected().getCurrentMp())
		{
			if (info.getSkill().isToggle())
			{
				info.getEffected().sendPacket(SystemMessageId.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
				return false;
			}
		}
		
		info.getEffected().reduceCurrentMp(manaDam);
		
		return info.getSkill().isToggle();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			info.getEffected().getActingPlayer().setRecentFakeDeath(true);
		}
		
		info.getEffected().broadcastPacket(new ChangeWaitType(info.getEffected(), ChangeWaitType.WT_STOP_FAKEDEATH));
		info.getEffected().broadcastPacket(new Revive(info.getEffected()));
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().startFakeDeath();
	}
}
