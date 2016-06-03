/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual.WindyHill;

import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Wind Vortex AI (Windy Hill)
 * @author malyelfik
 */
public final class WindVortex extends AbstractNpcAI
{
	// NPC
	private static final int VORTEX = 23417;
	private static final int GIANT_WINDIMA = 23419;
	private static final int IMMENSE_WINDIMA = 23420;
	
	public WindVortex()
	{
		addAttackId(VORTEX);
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isScriptValue(0) && !npc.isDead())
		{
			npc.setScriptValue(1);
			if (attacker.getRace().equals(Race.ERTHEIA))
			{
				final int npcId = (attacker.isMageClass()) ? IMMENSE_WINDIMA : GIANT_WINDIMA;
				showOnScreenMsg(attacker, NpcStringId.A_POWERFUL_MONSTER_HAS_COME_TO_FACE_YOU, ExShowScreenMessage.TOP_CENTER, 5000);
				addSpawn(npcId, npc, false, 120000);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new WindVortex();
	}
}