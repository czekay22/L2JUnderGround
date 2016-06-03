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
package org.l2junity.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.L2Spawn;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.TeleportWhereType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.DoorInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.olympiad.OlympiadGameTask;
import org.l2junity.gameserver.model.zone.AbstractZoneSettings;
import org.l2junity.gameserver.model.zone.L2ZoneRespawn;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.network.client.send.ExOlympiadMatchEnd;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * An olympiad stadium
 * @author durgus, DS
 */
public class OlympiadStadiumZone extends L2ZoneRespawn
{
	private final List<DoorInstance> _doors = new ArrayList<>(2);
	private final List<L2Spawn> _buffers = new ArrayList<>(2);
	private final List<Location> _spectatorLocations = new ArrayList<>(1);
	private int _instanceTemplate = 0;
	
	public OlympiadStadiumZone(int id)
	{
		super(id);
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new Settings();
		}
		setSettings(settings);
	}
	
	public final class Settings extends AbstractZoneSettings
	{
		private OlympiadGameTask _task = null;
		
		protected Settings()
		{
		}
		
		public OlympiadGameTask getOlympiadTask()
		{
			return _task;
		}
		
		protected void setTask(OlympiadGameTask task)
		{
			_task = task;
		}
		
		@Override
		public void clear()
		{
			_task = null;
		}
	}
	
	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}
	
	@Override
	public void parseLoc(int x, int y, int z, String type)
	{
		if ((type != null) && type.equals("spectatorSpawn"))
		{
			_spectatorLocations.add(new Location(x, y, z));
		}
		else
		{
			super.parseLoc(x, y, z, type);
		}
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("instanceTemplate"))
		{
			_instanceTemplate = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		getSettings().setTask(task);
	}
	
	@Override
	protected final void onEnter(Creature character)
	{
		if (getSettings().getOlympiadTask() != null)
		{
			if (getSettings().getOlympiadTask().isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character.isPlayer())
				{
					character.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
					getSettings().getOlympiadTask().getGame().sendOlympiadInfo(character);
				}
			}
		}
		
		if (character.isPlayable())
		{
			final PlayerInstance player = character.getActingPlayer();
			if (player != null)
			{
				// only participants, observers and GMs allowed
				if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && !player.isInOlympiadMode())
				{
					ThreadPoolManager.getInstance().executeGeneral(new KickPlayer(player));
				}
				else
				{
					// check for pet
					final Summon pet = player.getPet();
					if (pet != null)
					{
						pet.unSummon(player);
					}
				}
			}
		}
	}
	
	@Override
	protected final void onExit(Creature character)
	{
		if (getSettings().getOlympiadTask() != null)
		{
			if (getSettings().getOlympiadTask().isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character.isPlayer())
				{
					character.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	private static final class KickPlayer implements Runnable
	{
		private PlayerInstance _player;
		
		protected KickPlayer(PlayerInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player != null)
			{
				_player.getServitors().values().forEach(s -> s.unSummon(_player));
				_player.teleToLocation(TeleportWhereType.TOWN, null);
				_player = null;
			}
		}
	}
	
	public List<DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public List<L2Spawn> getBuffers()
	{
		return _buffers;
	}
	
	public List<Location> getSpectatorSpawns()
	{
		return _spectatorLocations;
	}
	
	/**
	 * Returns zone instanceTemplate
	 * @return
	 */
	public int getInstanceTemplateId()
	{
		return _instanceTemplate;
	}
}
