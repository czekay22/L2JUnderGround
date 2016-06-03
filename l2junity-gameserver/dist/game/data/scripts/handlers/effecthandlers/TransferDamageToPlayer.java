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
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.stats.Stats;

/**
 * Transfer Damage effect implementation.
 * @author UnAfraid
 */
public final class TransferDamageToPlayer extends AbstractStatAddEffect
{
	public TransferDamageToPlayer(StatsSet params)
	{
		super(params, Stats.TRANSFER_DAMAGE_TO_PLAYER);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isPlayable() && info.getEffector().isPlayer())
		{
			((Playable) info.getEffected()).setTransferDamageTo(null);
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayable() && info.getEffector().isPlayer())
		{
			((Playable) info.getEffected()).setTransferDamageTo(info.getEffector().getActingPlayer());
		}
	}
}