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
package quests.Q00035_FindGlitteringJewelry;

import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;

/**
 * Find Glittering Jewelry (35)
 * @author malyelfik
 */
public class Q00035_FindGlitteringJewelry extends Quest
{
	// NPCs
	private static final int ELLIE = 30091;
	private static final int FELTON = 30879;
	// Monster
	private static final int ALLIGATOR = 20135;
	// Items
	private static final int SILVER_NUGGET = 1873;
	private static final int ORIHARUKON = 1893;
	private static final int THONS = 4044;
	private static final int JEWEL_BOX = 7077;
	private static final int ROUGH_JEWEL = 7162;
	// Misc
	private static final int MIN_LEVEL = 60;
	private static final int JEWEL_COUNT = 10;
	private static final int ORIHARUKON_COUNT = 5;
	private static final int NUGGET_COUNT = 500;
	private static final int THONS_COUNT = 150;
	
	public Q00035_FindGlitteringJewelry()
	{
		super(35);
		addStartNpc(ELLIE);
		addTalkId(ELLIE, FELTON);
		addKillId(ALLIGATOR);
		registerQuestItems(ROUGH_JEWEL);
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
			case "30091-03.htm":
				st.startQuest();
				break;
			case "30879-02.html":
				st.setCond(2, true);
				break;
			case "30091-07.html":
				if (getQuestItemsCount(player, ROUGH_JEWEL) < JEWEL_COUNT)
				{
					return "30091-08.html";
				}
				takeItems(player, ROUGH_JEWEL, -1);
				st.setCond(4, true);
				break;
			case "30091-11.html":
				if ((getQuestItemsCount(player, ORIHARUKON) >= ORIHARUKON_COUNT) && (getQuestItemsCount(player, SILVER_NUGGET) >= NUGGET_COUNT) && (getQuestItemsCount(player, THONS) >= THONS_COUNT))
				{
					takeItems(player, ORIHARUKON, ORIHARUKON_COUNT);
					takeItems(player, SILVER_NUGGET, NUGGET_COUNT);
					takeItems(player, THONS, THONS_COUNT);
					giveItems(player, JEWEL_BOX, 1);
					st.exitQuest(false, true);
				}
				else
				{
					htmltext = "30091-12.html";
				}
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final PlayerInstance member = getRandomPartyMember(player, 2);
		if (member != null)
		{
			final QuestState st = getQuestState(member, false);
			if (getRandomBoolean())
			{
				giveItems(member, ROUGH_JEWEL, 1);
				if (getQuestItemsCount(member, ROUGH_JEWEL) >= JEWEL_COUNT)
				{
					st.setCond(3, true);
				}
				else
				{
					playSound(member, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
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
		
		switch (npc.getId())
		{
			case ELLIE:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getLevel() >= MIN_LEVEL) ? "30091-01.htm" : "30091-02.html";
						break;
					case State.STARTED:
						switch (st.getCond())
						{
							case 1:
								htmltext = "30091-04.html";
								break;
							case 3:
								htmltext = (getQuestItemsCount(player, ROUGH_JEWEL) >= JEWEL_COUNT) ? "30091-06.html" : "30091-05.html";
								break;
							case 4:
								htmltext = ((getQuestItemsCount(player, ORIHARUKON) >= ORIHARUKON_COUNT) && (getQuestItemsCount(player, SILVER_NUGGET) >= NUGGET_COUNT) && (getQuestItemsCount(player, THONS) >= THONS_COUNT)) ? "30091-09.html" : "30091-10.html";
								break;
						}
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case FELTON:
				if (st.isStarted())
				{
					if (st.isCond(1))
					{
						htmltext = "30879-01.html";
					}
					else if (st.isCond(2))
					{
						htmltext = "30879-03.html";
					}
				}
				break;
		}
		return htmltext;
	}
}