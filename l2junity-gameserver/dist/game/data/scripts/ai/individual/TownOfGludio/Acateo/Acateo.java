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
package ai.individual.TownOfGludio.Acateo;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

import ai.AbstractNpcAI;

/**
 * Acateo AI.
 * @author Gladicek
 */
public final class Acateo extends AbstractNpcAI
{
	// NPC
	private static final int ACATEO = 33905;
	// Item
	private static final int ACADEMY_CIRCLET = 8181;
	
	private Acateo()
	{
		addStartNpc(ACATEO);
		addFirstTalkId(ACATEO);
		addTalkId(ACATEO);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("give_circlet"))
		{
			if (hasQuestItems(player, ACADEMY_CIRCLET))
			{
				return "33905-3.html";
			}
			giveItems(player, ACADEMY_CIRCLET, 1);
			return "33905-2.html";
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return player.isAcademyMember() ? "33905-1.html" : "33905.html";
	}
	
	public static void main(String[] args)
	{
		new Acateo();
	}
}