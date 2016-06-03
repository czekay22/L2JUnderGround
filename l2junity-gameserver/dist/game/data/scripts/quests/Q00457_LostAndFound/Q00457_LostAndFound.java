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
package quests.Q00457_LostAndFound;

import java.util.Set;

import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.datatables.SpawnTable;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.enums.QuestType;
import org.l2junity.gameserver.model.L2Spawn;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.quest.State;
import org.l2junity.gameserver.network.client.send.CreatureSay;
import org.l2junity.gameserver.network.client.send.NpcSay;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

/**
 * Lost and Found (457)
 * @author nonom
 */
public final class Q00457_LostAndFound extends Quest
{
	// NPCs
	private static final int GUMIEL = 32759;
	private static final int ESCORT_CHECKER = 32764;
	private static final int[] SOLINA_CLAN =
	{
		22789, // Guide Solina
		22790, // Seeker Solina
		22791, // Savior Solina
		22793, // Ascetic Solina
	};
	// Misc
	private static final int PACKAGED_BOOK = 15716;
	private static final int CHANCE_SPAWN = 1; // 1%
	private static final int MIN_LV = 82;
	private static Set<L2Spawn> _escortCheckers;
	
	public Q00457_LostAndFound()
	{
		super(457);
		addStartNpc(GUMIEL);
		addSpawnId(ESCORT_CHECKER);
		addFirstTalkId(GUMIEL);
		addTalkId(GUMIEL);
		addKillId(SOLINA_CLAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32759-06.html":
			{
				npc.setScriptValue(0);
				st.startQuest();
				npc.setTarget(player);
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
				startQuestTimer("CHECK", 1000, npc, player, true);
				startQuestTimer("TIME_LIMIT", 600000, npc, player);
				startQuestTimer("TALK_TIME", 120000, npc, player);
				startQuestTimer("TALK_TIME2", 30000, npc, player);
				break;
			}
			case "TALK_TIME":
			{
				broadcastNpcSay(npc, player, NpcStringId.AH_I_THINK_I_REMEMBER_THIS_PLACE, false);
				break;
			}
			case "TALK_TIME2":
			{
				broadcastNpcSay(npc, player, NpcStringId.WHAT_WERE_YOU_DOING_HERE, false);
				startQuestTimer("TALK_TIME3", 10 * 1000, npc, player);
				break;
			}
			case "TALK_TIME3":
			{
				broadcastNpcSay(npc, player, NpcStringId.I_GUESS_YOU_RE_THE_SILENT_TYPE_THEN_ARE_YOU_LOOKING_FOR_TREASURE_LIKE_ME, false);
				break;
			}
			case "TIME_LIMIT":
			{
				startQuestTimer("STOP", 2000, npc, player);
				st.exitQuest(QuestType.DAILY);
				break;
			}
			case "CHECK":
			{
				final double distance = npc.calculateDistance(player, true, false);
				if (distance > 1000)
				{
					if (distance > 5000)
					{
						startQuestTimer("STOP", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
					else if (npc.isScriptValue(0))
					{
						broadcastNpcSay(npc, player, NpcStringId.HEY_DON_T_GO_SO_FAST, true);
						npc.setScriptValue(1);
					}
					else if (npc.isScriptValue(1))
					{
						broadcastNpcSay(npc, player, NpcStringId.IT_S_HARD_TO_FOLLOW, true);
						npc.setScriptValue(2);
					}
					else if (npc.isScriptValue(2))
					{
						startQuestTimer("STOP", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
				}
				for (L2Spawn escortSpawn : _escortCheckers)
				{
					final Npc escort = escortSpawn.getLastSpawn();
					if ((escort != null) && npc.isInsideRadius(escort, 1000, false, false))
					{
						startQuestTimer("STOP", 1000, npc, player);
						startQuestTimer("BYE", 3000, npc, player);
						cancelQuestTimer("CHECK", npc, player);
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getName(), NpcStringId.AH_FRESH_AIR));
						broadcastNpcSay(npc, player, NpcStringId.AH_FRESH_AIR, false);
						giveItems(player, PACKAGED_BOOK, 1);
						st.exitQuest(QuestType.DAILY, true);
						break;
					}
				}
				break;
			}
			case "STOP":
			{
				npc.setTarget(null);
				npc.getAI().stopFollow();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				cancelQuestTimer("CHECK", npc, player);
				cancelQuestTimer("TIME_LIMIT", npc, player);
				cancelQuestTimer("TALK_TIME", npc, player);
				cancelQuestTimer("TALK_TIME2", npc, player);
				break;
			}
			case "BYE":
			{
				npc.deleteMe();
				break;
			}
			default:
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		if (npc.getTarget() != null)
		{
			return npc.getTarget().equals(player) ? "32759-08.html" : "32759-01a.html";
		}
		return "32759.html";
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final QuestState st = getQuestState(player, true);
		
		if ((getRandom(100) < CHANCE_SPAWN) && st.isNowAvailable() && (player.getLevel() >= MIN_LV))
		{
			addSpawn(GUMIEL, npc);
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
			st = newQuestState(player);
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() >= MIN_LV) ? "32759-01.htm" : "32759-03.html";
				break;
			}
			case State.COMPLETED:
			{
				if (st.isNowAvailable())
				{
					st.setState(State.CREATED);
					htmltext = (player.getLevel() >= MIN_LV) ? "32759-01.htm" : "32759-03.html";
				}
				else
				{
					htmltext = "32759-02.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		_escortCheckers = SpawnTable.getInstance().getSpawns(ESCORT_CHECKER);
		return super.onSpawn(npc);
	}
	
	public void broadcastNpcSay(Npc npc, PlayerInstance player, NpcStringId stringId, boolean whisper)
	{
		((whisper) ? player : npc).sendPacket(new NpcSay(npc.getObjectId(), ((whisper) ? ChatType.NPC_WHISPER : ChatType.NPC_GENERAL), npc.getId(), stringId));
	}
}