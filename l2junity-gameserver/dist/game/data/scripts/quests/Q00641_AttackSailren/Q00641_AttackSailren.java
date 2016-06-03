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
package quests.Q00641_AttackSailren;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

import quests.Q00126_TheNameOfEvil2.Q00126_TheNameOfEvil2;

/**
 * Attack Sailren! (641)
 * @author Adry_85
 */
public class Q00641_AttackSailren extends Quest
{
	// NPC
	private static final int SHILENS_STONE_STATUE = 32109;
	// Items
	public static final int GAZKH_FRAGMENT = 8782;
	public static final int GAZKH = 8784;
	
	public static int[] MOBS =
	{
		22196, // Velociraptor
		22197, // Velociraptor
		22198, // Velociraptor
		22218, // Velociraptor
		22223, // Velociraptor
		22199, // Pterosaur
	};
	
	public Q00641_AttackSailren()
	{
		super(641);
		addStartNpc(SHILENS_STONE_STATUE);
		addTalkId(SHILENS_STONE_STATUE);
		addKillId(MOBS);
		registerQuestItems(GAZKH_FRAGMENT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "32109-1.html":
				st.startQuest();
				break;
			case "32109-2a.html":
				if (getQuestItemsCount(player, GAZKH_FRAGMENT) >= 30)
				{
					giveItems(player, GAZKH, 1);
					st.exitQuest(true, true);
				}
				break;
		}
		return event;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final PlayerInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember != null)
		{
			final QuestState st = getQuestState(partyMember, false);
			if (st != null)
			{
				giveItems(player, GAZKH_FRAGMENT, 1);
				if (getQuestItemsCount(player, GAZKH_FRAGMENT) < 30)
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else
				{
					st.setCond(2, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 77)
				{
					htmltext = "32109-0.htm";
				}
				else
				{
					st = player.getQuestState(Q00126_TheNameOfEvil2.class.getSimpleName());
					htmltext = ((st != null) && st.isCompleted()) ? "32109-0a.htm" : "32109-0b.htm";
				}
				break;
			case State.STARTED:
				htmltext = (st.isCond(1)) ? "32109-1a.html" : "32109-2.html";
				break;
		}
		return htmltext;
	}
}
