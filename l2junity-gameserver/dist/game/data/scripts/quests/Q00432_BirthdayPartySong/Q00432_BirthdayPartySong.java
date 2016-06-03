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
package quests.Q00432_BirthdayPartySong;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Birthday Party Song (432)
 * @author malyelfik
 */
public final class Q00432_BirthdayPartySong extends Quest
{
	// NPC
	private static final int OCTAVIA = 31043;
	// Monster
	private static final int GOLEM = 21103;
	// Item
	private static final int RED_CRYSTAL = 7541;
	private static final int ECHO_CRYSTAL = 7061;
	// Misc
	private static final int MIN_LVL = 31;
	
	public Q00432_BirthdayPartySong()
	{
		super(432);
		addStartNpc(OCTAVIA);
		addTalkId(OCTAVIA);
		addKillId(GOLEM);
		registerQuestItems(RED_CRYSTAL);
		addCondMinLevel(MIN_LVL, "31043-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "31043-02.htm":
			{
				st.startQuest();
				break;
			}
			case "31043-05.html":
			{
				if (getQuestItemsCount(player, RED_CRYSTAL) < 50)
				{
					return "31043-06.html";
				}
				giveItems(player, ECHO_CRYSTAL, 25);
				st.exitQuest(true, true);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final QuestState st = getQuestState(player, false);
		
		if ((st != null) && st.isCond(1) && getRandomBoolean())
		{
			giveItems(player, RED_CRYSTAL, 1);
			if (getQuestItemsCount(player, RED_CRYSTAL) == 50)
			{
				st.setCond(2, true);
			}
			else
			{
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = "31043-01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (st.isCond(1)) ? "31043-03.html" : "31043-04.html";
				break;
			}
		}
		return htmltext;
	}
}