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
package ai.servitors;

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

import ai.AbstractNpcAI;

/**
 * Tree of Life AI.
 * @author St3eT.
 */
public final class TreeOfLife extends AbstractNpcAI
{
	// NPCs
	private static final int[] TREE_OF_LIFE =
	{
		14933,
		14943,
		15010,
		15011,
		15154,
	};
	
	private TreeOfLife()
	{
		addSummonSpawnId(TREE_OF_LIFE);
	}
	
	@Override
	public void onSummonSpawn(Summon summon)
	{
		getTimers().addTimer("HEAL", 3000, null, summon.getOwner());
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		if (player != null)
		{
			final Summon summon = player.getFirstServitor();
			if (event.equals("HEAL") && (summon != null) && CommonUtil.contains(TREE_OF_LIFE, summon.getId()))
			{
				summon.doCast(summon.getTemplate().getParameters().getSkillHolder("s_tree_heal").getSkill());
				getTimers().addTimer("HEAL", 8000, null, player);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new TreeOfLife();
	}
}