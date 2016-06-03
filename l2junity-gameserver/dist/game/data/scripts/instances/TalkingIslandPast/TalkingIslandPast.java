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
package instances.TalkingIslandPast;

import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureSee;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.network.client.send.ExShowScreenMessage;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;

import instances.AbstractInstance;
import quests.Q10385_RedThreadOfFate.Q10385_RedThreadOfFate;

/**
 * Talking Island (Past) instance zone.
 * @author Gladicek
 */
public final class TalkingIslandPast extends AbstractInstance
{
	// NPCs
	private static final int DARIN = 33748;
	private static final int ROXXY = 33749;
	private static final int MYSTERIOUS_DARK_KNIGHT = 33751;
	private static final int INVISIBLE_TI_NPC = 18919;
	// Misc
	private static final int TEMPLATE_ID = 241;
	// Location
	private static final Location TOWN_TELEPORT = new Location(210799, 13426, -3720);
	private static final Location TI_LOC_1 = new Location(210779, 15547, -3732);
	private static final Location TI_LOC_2 = new Location(209267, 14943, -3729);
	private static final Location TI_LOC_3 = new Location(210332, 13156, -3729);
	// Zones
	private static final int TALKING_ISLAND_ZONE = 12035;
	
	public TalkingIslandPast()
	{
		addTalkId(DARIN, ROXXY, MYSTERIOUS_DARK_KNIGHT);
		addFirstTalkId(DARIN, ROXXY, MYSTERIOUS_DARK_KNIGHT);
		addExitZoneId(TALKING_ISLAND_ZONE);
		addSpawnId(INVISIBLE_TI_NPC);
		setCreatureSeeId(this::onCreatureSee, INVISIBLE_TI_NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("enterInstance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
		}
		else if (event.equals("exitInstance"))
		{
			final Instance world = getPlayerInstance(player);
			if (world != null)
			{
				teleportPlayerOut(player, world);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	protected void onEnter(PlayerInstance player, Instance instance, boolean firstEnter)
	{
		final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
		if ((qs != null) && qs.isCond(21) && (qs.isMemoState(2)))
		{
			Npc knight = addSpawn(MYSTERIOUS_DARK_KNIGHT, TI_LOC_3, false, 0, false, instance.getId());
			knight.getAI().startFollow(player);
			knight.setIsRunning(true);
			showOnScreenMsg(player, NpcStringId.A_MYSTERIOUS_DARK_KNIGHT_IS_HERE, ExShowScreenMessage.TOP_CENTER, 5000);
			getTimers().addTimer("MSG", null, 5000, null, player, n -> showOnScreenMsg(n.getPlayer(), NpcStringId.TALK_TO_THE_MYSTERIOUS_DARK_KNIGHT, ExShowScreenMessage.TOP_CENTER, 5000));
		}
		super.onEnter(player, instance, firstEnter);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.initSeenCreatures();
		return super.onSpawn(npc);
	}
	
	@Override
	public String onExitZone(Creature character, ZoneType zone)
	{
		final Instance instance = character.getInstanceWorld();
		if ((instance != null) && character.isPlayer())
		{
			character.teleToLocation(TOWN_TELEPORT);
		}
		return super.onExitZone(character, zone);
	}
	
	public void onCreatureSee(OnCreatureSee event)
	{
		final Creature creature = event.getSeen();
		final Npc npc = (Npc) event.getSeer();
		
		if (creature.isPlayer())
		{
			final Instance instance = creature.getInstanceWorld();
			final PlayerInstance player = creature.getActingPlayer();
			final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
			
			if ((instance != null) && (npc.getId() == INVISIBLE_TI_NPC) && (qs != null) && qs.isCond(21) && qs.isMemoState(1))
			{
				final Location loc = npc.isInsideRadius(TI_LOC_1, 1000, false, false) ? TI_LOC_1 : TI_LOC_2;
				qs.setMemoState(2);
				final Npc knight = addSpawn(MYSTERIOUS_DARK_KNIGHT, loc, false, 0, false, instance.getId());
				knight.getAI().startFollow(player);
				knight.setIsRunning(true);
				showOnScreenMsg(player, NpcStringId.A_MYSTERIOUS_DARK_KNIGHT_IS_HERE, ExShowScreenMessage.TOP_CENTER, 5000);
				getTimers().addTimer("MSG", null, 5000, npc, player, n -> showOnScreenMsg(n.getPlayer(), NpcStringId.TALK_TO_THE_MYSTERIOUS_DARK_KNIGHT, ExShowScreenMessage.TOP_CENTER, 5000));
				
			}
		}
	}
	
	public static void main(String[] args)
	{
		new TalkingIslandPast();
	}
}
