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
package ai.individual.WharfGludioAirships.FortuneTelling;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.Inventory;

import ai.AbstractNpcAI;

/**
 * Fortune Telling AI.
 * @author Nyaran
 */
public final class FortuneTelling extends AbstractNpcAI
{
	// NPC
	private static final int MINE = 32616;
	// Misc
	private static final int COST = 1000;
	
	public FortuneTelling()
	{
		addStartNpc(MINE);
		addTalkId(MINE);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		if (player.getAdena() < COST)
		{
			htmltext = "lowadena.htm";
		}
		else
		{
			takeItems(player, Inventory.ADENA_ID, COST);
			htmltext = getHtm(player.getHtmlPrefix(), "fortune.htm").replace("%fortune%", String.valueOf(getRandom(1800309, 1800695)));
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new FortuneTelling();
	}
}