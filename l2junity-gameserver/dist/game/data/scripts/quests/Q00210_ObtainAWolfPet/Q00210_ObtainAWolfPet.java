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
package quests.Q00210_ObtainAWolfPet;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Obtain a Wolf Pet (210)
 * @author Gladicek
 */
public final class Q00210_ObtainAWolfPet extends Quest
{
	// NPCs
	private static final int LUNDY = 30827;
	private static final int BELLA = 30256;
	private static final int BRYNN = 30335;
	private static final int SYDNIA = 30321;
	// Item
	private static final int WOLF_COLLAR = 2375;
	// Misc
	private static final int MIN_LEVEL = 15;
	
	public Q00210_ObtainAWolfPet()
	{
		super(210);
		addStartNpc(LUNDY);
		addTalkId(LUNDY, BELLA, BRYNN, SYDNIA);
		addCondMinLevel(MIN_LEVEL, "30827-07.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30827-02.htm":
			case "30256-02.htm":
			case "30256-03.htm":
			case "30335-02.htm":
			case "30321-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30827-03.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30827-06.htm":
			{
				if (qs.isCond(4))
				{
					giveItems(player, WOLF_COLLAR, 1);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == LUNDY)
				{
					htmltext = "30827-01.htm";
					break;
				}
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case LUNDY:
					{
						if (qs.isCond(1))
						{
							htmltext = "30827-04.htm";
							break;
						}
						else if (qs.isCond(4))
						{
							htmltext = "30827-05.htm";
							break;
						}
						break;
					}
					case BELLA:
					{
						if (qs.isCond(1))
						{
							qs.setCond(2, true);
							htmltext = "30256-01.htm";
						}
						break;
					}
					case BRYNN:
					{
						if (qs.isCond(2))
						{
							qs.setCond(3, true);
							htmltext = "30335-01.htm";
						}
						break;
					}
					case SYDNIA:
					{
						if (qs.isCond(3))
						{
							qs.setCond(4, true);
							htmltext = "30321-01.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == LUNDY)
				{
					htmltext = getAlreadyCompletedMsg(player);
					break;
				}
				break;
			}
		}
		return htmltext;
	}
}