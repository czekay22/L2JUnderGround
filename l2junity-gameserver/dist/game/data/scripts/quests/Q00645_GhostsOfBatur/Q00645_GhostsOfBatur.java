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
package quests.Q00645_GhostsOfBatur;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.util.Util;

/**
 * Ghosts of Batur (645)
 * @author Zoey76
 */
public class Q00645_GhostsOfBatur extends Quest
{
	// NPC
	private static final int KARUDA = 32017;
	// Monsters
	private static final int CONTAMINATED_MOREK_WARRIOR = 22703;
	private static final int CONTAMINATED_BATUR_WARRIOR = 22704;
	private static final int CONTAMINATED_BATUR_COMMANDER = 22705;
	// Items
	private static final int CURSED_GRAVE_GOODS = 8089; // Old item
	private static final int CURSED_BURIAL_ITEMS = 14861; // New item
	// Misc
	private static final int MIN_LEVEL = 80;
	private static final int[] CHANCES =
	{
		516,
		664,
		686
	};
	
	public Q00645_GhostsOfBatur()
	{
		super(645);
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		addKillId(CONTAMINATED_MOREK_WARRIOR, CONTAMINATED_BATUR_WARRIOR, CONTAMINATED_BATUR_COMMANDER);
		registerQuestItems(CURSED_GRAVE_GOODS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return null;
		}
		
		String htmltext = null;
		if (player.getLevel() >= MIN_LEVEL)
		{
			switch (event)
			{
				case "32017-03.htm":
				{
					st.startQuest();
					htmltext = event;
					break;
				}
				case "32017-06.html":
				case "32017-08.html":
				{
					htmltext = event;
					break;
				}
				case "32017-09.html":
				{
					st.exitQuest(true, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final PlayerInstance player = getRandomPartyMember(killer, 1);
		if ((player != null) && Util.checkIfInRange(1500, npc, player, false))
		{
			if (getRandom(1000) < CHANCES[npc.getId() - CONTAMINATED_MOREK_WARRIOR])
			{
				final QuestState st = getQuestState(player, false);
				giveItems(player, CURSED_BURIAL_ITEMS, 1);
				if (st.isCond(1) && (getQuestItemsCount(player, CURSED_BURIAL_ITEMS) >= 500))
				{
					st.setCond(2, true);
				}
				else
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = getNoQuestMsg(player);
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() >= MIN_LEVEL) ? "32017-01.htm" : "32017-02.html";
				break;
			}
			case State.STARTED:
			{
				// Support for old quest reward.
				final long count = getQuestItemsCount(player, CURSED_GRAVE_GOODS);
				if ((count > 0) && (count < 180))
				{
					giveAdena(player, 56000 + (count * 64), false);
					addExpAndSp(player, 138000, 7997);
					st.exitQuest(true, true);
					htmltext = "32017-07.html";
				}
				else
				{
					htmltext = hasQuestItems(player, CURSED_BURIAL_ITEMS) ? "32017-04.html" : "32017-05.html";
				}
				break;
			}
		}
		return htmltext;
	}
}
