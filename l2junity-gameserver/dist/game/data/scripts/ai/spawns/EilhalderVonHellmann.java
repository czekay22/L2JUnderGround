/*
 * Copyright (C) 2004-2015 L2J Unity
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
package ai.spawns;

import org.l2junity.gameserver.GameTimeController;
import org.l2junity.gameserver.instancemanager.DBSpawnManager;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.OnDayNightChange;
import org.l2junity.gameserver.model.spawns.NpcSpawnTemplate;
import org.l2junity.gameserver.model.spawns.SpawnGroup;
import org.l2junity.gameserver.model.spawns.SpawnTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.AbstractNpcAI;

/**
 * @author UnAfraid
 */
public final class EilhalderVonHellmann extends AbstractNpcAI
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EilhalderVonHellmann.class);
	private static final int EILHALDER_VON_HELLMANN = 25328;
	private NpcSpawnTemplate _template;
	
	private EilhalderVonHellmann()
	{
		addSpawnId(EILHALDER_VON_HELLMANN);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		// Spawn that comes from RaidBossSpawnManager
		if ((npc.getSpawn() == null) || (npc.getSpawn().getNpcSpawnTemplate() == null))
		{
			getTimers().addTimer("delete", 1000, event -> npc.deleteMe());
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public void onSpawnNpc(SpawnTemplate template, SpawnGroup group, Npc npc)
	{
		LOGGER.info("Spawning Night Raid Boss " + npc.getName());
		DBSpawnManager.getInstance().notifySpawnNightNpc(npc);
	}
	
	@Override
	public void onSpawnDespawnNpc(SpawnTemplate template, SpawnGroup group, Npc npc)
	{
		LOGGER.info("Despawning Night Raid Boss " + npc.getName());
	}
	
	@Override
	public void onSpawnActivate(SpawnTemplate template)
	{
		OUT: for (SpawnGroup group : template.getGroups())
		{
			for (NpcSpawnTemplate npc : group.getSpawns())
			{
				if (npc.getId() == EILHALDER_VON_HELLMANN)
				{
					_template = npc;
					break OUT;
				}
			}
		}
		
		handleBoss(GameTimeController.getInstance().isNight());
	}
	
	@RegisterEvent(EventType.ON_DAY_NIGHT_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void onDayNightChange(OnDayNightChange event)
	{
		handleBoss(event.isNight());
	}
	
	/**
	 * @param isNight
	 */
	private void handleBoss(boolean isNight)
	{
		if (_template == null)
		{
			return;
		}
		
		if (isNight)
		{
			_template.spawn(null);
		}
		else
		{
			_template.despawn();
		}
	}
	
	public static void main(String[] args)
	{
		new EilhalderVonHellmann();
	}
}
