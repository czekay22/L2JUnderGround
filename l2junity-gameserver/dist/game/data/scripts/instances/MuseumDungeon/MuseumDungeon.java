/*
 * Copyright (C) 2004-2016 L2J Unity
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
package instances.MuseumDungeon;

import java.util.List;

import org.l2junity.commons.util.CommonUtil;
import org.l2junity.gameserver.enums.ChatType;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.Id;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureAttacked;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureDeath;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import instances.AbstractInstance;
import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * Museum Dungeon Instance Zone.
 * @author Gladicek
 */
public final class MuseumDungeon extends AbstractInstance
{
	// NPC's
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	private static final int DESK = 33126;
	private static final int THIEF = 23121;
	// Items
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Misc
	private static final int TEMPLATE_ID = 182;
	
	private static final NpcStringId[] THIEF_SHOUT =
	{
		NpcStringId.YOU_LL_NEVER_LEAVE_WITH_THAT_BOOK,
		NpcStringId.FINALLY_I_THOUGHT_I_WAS_GOING_TO_DIE_WAITING
	};
	
	public MuseumDungeon()
	{
		super(TEMPLATE_ID);
		addStartNpc(PANTHEON);
		addFirstTalkId(DESK);
		addTalkId(PANTHEON, TOYRON);
		addSkillSeeId(THIEF);
	}
	
	@Override
	protected void onEnter(PlayerInstance player, Instance instance, boolean firstEnter)
	{
		super.onEnter(player, instance, firstEnter);
		
		final Attackable toyron = (Attackable) instance.getNpc(TOYRON);
		
		if (firstEnter)
		{
			// Set desk status
			final List<Npc> desks = instance.getNpcs(DESK);
			final Npc desk = desks.get(getRandom(desks.size()));
			desk.getVariables().set("book", true);
			
			// Set Toyron
			toyron.setIsRunning(true);
			toyron.setCanReturnToSpawnPoint(false);
		}
		
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		if (qs != null)
		{
			if (qs.isCond(1))
			{
				showOnScreenMsg(player, NpcStringId.AMONG_THE_4_BOOKSHELVES_FIND_THE_ONE_CONTAINING_A_VOLUME_CALLED_THE_WAR_OF_GODS_AND_GIANTS, ExShowScreenMessage.TOP_CENTER, 4500);
			}
			else if (qs.isCond(2))
			{
				getTimers().addTimer("TOYRON_FOLLOW", 1500, toyron, player);
				
				if (instance.getNpcs(THIEF).isEmpty())
				{
					instance.spawnGroup("thiefs").forEach(npc -> npc.setIsRunning(true));
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("enter_instance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, Npc npc, PlayerInstance player)
	{
		final Instance instance = npc.getInstanceWorld();
		final Attackable toyron = (Attackable) instance.getNpc(TOYRON);
		if (isInInstance(instance))
		{
			switch (event)
			{
				case "TOYRON_FOLLOW":
					toyron.getAI().startFollow(player);
					break;
				case "SPAWN_THIEFS_STAGE_1":
				{
					instance.spawnGroup("thiefs").forEach(thief ->
					{
						thief.setIsRunning(true);
						addAttackPlayerDesire(thief, player);
						thief.broadcastSay(ChatType.NPC_GENERAL, THIEF_SHOUT[getRandom(2)]);
					});
					toyron.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WHEN_DID_THEY_GET_IN_HERE);
					getTimers().addRepeatingTimer("TOYRON_MSG_1", 10000, toyron, player);
					getTimers().addRepeatingTimer("TOYRON_MSG_2", 12500, toyron, player);
					getTimers().addTimer("SKILL_MSG", 3500, toyron, player);
					break;
				}
				case "SKILL_MSG":
					showOnScreenMsg(player, NpcStringId.USE_YOUR_SKILL_ATTACKS_AGAINST_THEM, ExShowScreenMessage.TOP_CENTER, 4500);
					break;
				case "TOYRON_MSG_1":
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOUR_NORMAL_ATTACKS_AREN_T_WORKING);
					break;
				case "TOYRON_MSG_2":
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.LOOKS_LIKE_ONLY_SKILL_BASED_ATTACKS_DAMAGE_THEM);
					break;
			}
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		final Instance instance = npc.getInstanceWorld();
		String htmltext = null;
		if (isInInstance(instance))
		{
			final Npc toyron = instance.getNpc(TOYRON);
			
			final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
			if ((qs == null) || qs.isCond(2))
			{
				htmltext = "33126.html";
			}
			else if (qs.isCond(1))
			{
				if (npc.getVariables().getBoolean("book", false) && !hasQuestItems(player, THE_WAR_OF_GODS_AND_GIANTS))
				{
					qs.setCond(2);
					giveItems(player, THE_WAR_OF_GODS_AND_GIANTS, 1);
					showOnScreenMsg(player, NpcStringId.WATCH_OUT_YOU_ARE_BEING_ATTACKED, ExShowScreenMessage.TOP_CENTER, 4500);
					getTimers().addTimer("SPAWN_THIEFS_STAGE_1", 1000, npc, player);
					getTimers().addTimer("TOYRON_FOLLOW", 1000, toyron, player);
					htmltext = "33126-01.html";
				}
				else
				{
					htmltext = "33126-02.html";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSkillSee(Npc npc, PlayerInstance player, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		final Instance instance = npc.getInstanceWorld();
		
		if (isInInstance(instance))
		{
			if ((npc.getId() == THIEF) && skill.isBad() && (CommonUtil.contains(targets, npc)))
			{
				final Npc toyron = instance.getNpc(TOYRON);
				toyron.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ENOUGH_OF_THIS_COME_AT_ME);
				addAttackDesire(toyron, npc);
				npc.setScriptValue(1);
				getTimers().addTimer("TOYRON_FOLLOW", 3000, toyron, player);
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_ATTACKED)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(THIEF)
	public void onCreatureAttacked(OnCreatureAttacked event)
	{
		final Creature creature = event.getAttacker();
		final Npc npc = (Npc) event.getTarget();
		final Instance instance = npc.getInstanceWorld();
		
		if (isInInstance(instance) && !creature.isPlayer() && npc.isScriptValue(1))
		{
			getTimers().addTimer("THIEF_DIE", 1500, npc, null);
		}
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(THIEF)
	public void onCreatureKill(OnCreatureDeath event)
	{
		final Npc npc = (Npc) event.getTarget();
		final Instance instance = npc.getInstanceWorld();
		if (isInInstance(instance))
		{
			final Attackable toyron = (Attackable) instance.getNpc(TOYRON);
			
			final PlayerInstance player = instance.getFirstPlayer();
			final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
			if ((qs != null) && qs.isCond(2) && instance.getAliveNpcs(THIEF).isEmpty())
			{
				qs.setCond(3, true);
				getTimers().cancelTimer("TOYRON_MSG_1", toyron, player);
				getTimers().cancelTimer("TOYRON_MSG_2", toyron, player);
				showOnScreenMsg(player, NpcStringId.TALK_TO_TOYRON_TO_RETURN_TO_THE_MUSEUM_LOBBY, ExShowScreenMessage.TOP_CENTER, 4500);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new MuseumDungeon();
	}
}