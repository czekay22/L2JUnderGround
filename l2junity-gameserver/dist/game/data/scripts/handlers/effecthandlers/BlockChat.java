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

import org.l2junity.gameserver.instancemanager.PunishmentManager;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.effects.EffectFlag;
import org.l2junity.gameserver.model.punishment.PunishmentAffect;
import org.l2junity.gameserver.model.punishment.PunishmentTask;
import org.l2junity.gameserver.model.punishment.PunishmentType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;

/**
 * Block Chat effect implementation.
 * @author BiggBoss
 */
public final class BlockChat extends AbstractEffect
{
	public BlockChat(StatsSet params)
	{
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (info.getEffected() != null) && info.getEffected().isPlayer();
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.CHAT_BLOCK.getMask();
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(0, effected.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN, 0, "Chat banned bot report", "system", true));
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		PunishmentManager.getInstance().stopPunishment(info.getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN);
	}
}
