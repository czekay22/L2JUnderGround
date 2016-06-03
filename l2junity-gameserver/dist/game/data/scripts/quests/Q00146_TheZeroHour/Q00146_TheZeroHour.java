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
package quests.Q00146_TheZeroHour;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

import quests.Q00109_InSearchOfTheNest.Q00109_InSearchOfTheNest;

/**
 * The Zero Hour (146)
 * @author Gnacik, malyelfik
 */
public class Q00146_TheZeroHour extends Quest
{
	// NPCs
	private static final int KAHMAN = 31554;
	private static final int QUEEN_SHYEED = 25671;
	// Item
	private static final int KAHMANS_SUPPLY_BOX = 14849;
	private static final int FANG = 14859;
	
	public Q00146_TheZeroHour()
	{
		super(146);
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		addKillId(QUEEN_SHYEED);
		registerQuestItems(FANG);
	}
	
	@Override
	public int getNpcStringId()
	{
		return 640;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (event.equalsIgnoreCase("31554-03.htm"))
		{
			st.startQuest();
		}
		return event;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final PlayerInstance partyMember = getRandomPartyMember(killer, 1);
		if (partyMember != null)
		{
			final QuestState st = getQuestState(partyMember, false);
			if (!hasQuestItems(partyMember, FANG))
			{
				giveItems(partyMember, FANG, 1);
				st.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
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
				if (player.getLevel() < 81)
				{
					htmltext = "31554-02.htm";
				}
				else
				{
					final QuestState prev = player.getQuestState(Q00109_InSearchOfTheNest.class.getSimpleName());
					if ((prev != null) && prev.isCompleted())
					{
						htmltext = "31554-01a.htm";
					}
					else
					{
						htmltext = "31554-04.html";
					}
				}
				break;
			case State.STARTED:
				if (st.isCond(1))
				{
					htmltext = "31554-06.html";
				}
				else
				{
					giveItems(player, KAHMANS_SUPPLY_BOX, 1);
					addExpAndSp(player, 154616, 12500);
					st.exitQuest(false, true);
					htmltext = "31554-05.html";
				}
				break;
			case State.COMPLETED:
				htmltext = "31554-01b.htm";
				break;
		}
		return htmltext;
	}
}
