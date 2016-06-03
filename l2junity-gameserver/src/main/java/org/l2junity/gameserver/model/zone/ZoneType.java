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
package org.l2junity.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.TeleportWhereType;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.ListenersContainer;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureZoneEnter;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureZoneExit;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for any zone type handles basic operations.
 * @author durgus
 */
public abstract class ZoneType extends ListenersContainer
{
	protected static final Logger _log = LoggerFactory.getLogger(ZoneType.class);
	
	private final int _id;
	protected L2ZoneForm _zone;
	protected Map<Integer, Creature> _characterList = new ConcurrentHashMap<>();
	
	/** Parameters to affect specific characters */
	private boolean _checkAffected = false;
	private String _name = null;
	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;
	private InstanceType _target = InstanceType.L2Character; // default all chars
	private boolean _allowStore;
	private boolean _enabled;
	private AbstractZoneSettings _settings;
	
	protected ZoneType(int id)
	{
		_id = id;
		
		_minLvl = 0;
		_maxLvl = 0xFF;
		
		_classType = 0;
		
		_race = null;
		_class = null;
		_allowStore = true;
		_enabled = true;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Setup new parameters for this zone
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;
		
		// Zone name
		if (name.equals("name"))
		{
			_name = value;
		}
		// Minimum level
		else if (name.equals("affectedLvlMin"))
		{
			_minLvl = Integer.parseInt(value);
		}
		// Maximum level
		else if (name.equals("affectedLvlMax"))
		{
			_maxLvl = Integer.parseInt(value);
		}
		// Affected Races
		else if (name.equals("affectedRace"))
		{
			// Create a new array holding the affected race
			if (_race == null)
			{
				_race = new int[1];
				_race[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_race.length + 1];
				
				int i = 0;
				for (; i < _race.length; i++)
				{
					temp[i] = _race[i];
				}
				
				temp[i] = Integer.parseInt(value);
				
				_race = temp;
			}
		}
		// Affected classes
		else if (name.equals("affectedClassId"))
		{
			// Create a new array holding the affected classIds
			if (_class == null)
			{
				_class = new int[1];
				_class[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_class.length + 1];
				
				int i = 0;
				for (; i < _class.length; i++)
				{
					temp[i] = _class[i];
				}
				
				temp[i] = Integer.parseInt(value);
				
				_class = temp;
			}
		}
		// Affected class type
		else if (name.equals("affectedClassType"))
		{
			if (value.equals("Fighter"))
			{
				_classType = 1;
			}
			else
			{
				_classType = 2;
			}
		}
		else if (name.equals("targetClass"))
		{
			_target = Enum.valueOf(InstanceType.class, value);
		}
		else if (name.equals("allowStore"))
		{
			_allowStore = Boolean.parseBoolean(value);
		}
		else if (name.equals("default_enabled"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + getId());
		}
	}
	
	/**
	 * @param character the player to verify.
	 * @return {@code true} if the given character is affected by this zone, {@code false} otherwise.
	 */
	private boolean isAffected(Creature character)
	{
		// Check lvl
		if ((character.getLevel() < _minLvl) || (character.getLevel() > _maxLvl))
		{
			return false;
		}
		
		// check obj class
		if (!character.isInstanceTypes(_target))
		{
			return false;
		}
		
		if (character instanceof PlayerInstance)
		{
			// Check class type
			if (_classType != 0)
			{
				if (((PlayerInstance) character).isMageClass())
				{
					if (_classType == 1)
					{
						return false;
					}
				}
				else if (_classType == 2)
				{
					return false;
				}
			}
			
			// Check race
			if (_race != null)
			{
				boolean ok = false;
				
				for (int element : _race)
				{
					if (character.getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
			
			// Check class
			if (_class != null)
			{
				boolean ok = false;
				
				for (int _clas : _class)
				{
					if (((PlayerInstance) character).getClassId().ordinal() == _clas)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Set the zone for this L2ZoneType Instance
	 * @param zone
	 */
	public void setZone(L2ZoneForm zone)
	{
		if (_zone != null)
		{
			throw new IllegalStateException("Zone already set");
		}
		_zone = zone;
	}
	
	/**
	 * Returns this zones zone form.
	 * @return {@link #_zone}
	 */
	public L2ZoneForm getZone()
	{
		return _zone;
	}
	
	/**
	 * Set the zone name.
	 * @param name
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * Returns zone name
	 * @return
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Checks if the given coordinates are within zone's plane
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isInsideZone(int x, int y)
	{
		return _zone.isInsideZone(x, y, _zone.getHighZ());
	}
	
	/**
	 * Checks if the given coordinates are within the zone, ignores instanceId check
	 * @param loc
	 * @return
	 */
	public boolean isInsideZone(ILocational loc)
	{
		return _zone.isInsideZone(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Checks if the given coordinates are within the zone, ignores instanceId check
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Checks if the given object is inside the zone.
	 * @param object
	 * @return
	 */
	public boolean isInsideZone(WorldObject object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return getZone().getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(WorldObject object)
	{
		return getZone().getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(Creature character)
	{
		// If the character can't be affected by this zone return
		if (_checkAffected)
		{
			if (!isAffected(character))
			{
				return;
			}
		}
		
		// If the object is inside the zone...
		if (isInsideZone(character))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.containsKey(character.getObjectId()))
			{
				// Notify to scripts.
				EventDispatcher.getInstance().notifyEventAsync(new OnCreatureZoneEnter(character, this), this);
				
				// Register player.
				_characterList.put(character.getObjectId(), character);
				
				// Notify Zone implementation.
				onEnter(character);
			}
		}
		else
		{
			removeCharacter(character);
		}
	}
	
	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 * @param character
	 */
	public void removeCharacter(Creature character)
	{
		// Was the character inside this zone?
		if (_characterList.containsKey(character.getObjectId()))
		{
			// Notify to scripts.
			EventDispatcher.getInstance().notifyEventAsync(new OnCreatureZoneExit(character, this), this);
			
			// Unregister player.
			_characterList.remove(character.getObjectId());
			
			// Notify Zone implementation.
			onExit(character);
		}
	}
	
	/**
	 * Will scan the zones char list for the character
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(Creature character)
	{
		return _characterList.containsKey(character.getObjectId());
	}
	
	public AbstractZoneSettings getSettings()
	{
		return _settings;
	}
	
	public void setSettings(AbstractZoneSettings settings)
	{
		if (_settings != null)
		{
			_settings.clear();
		}
		_settings = settings;
	}
	
	protected abstract void onEnter(Creature character);
	
	protected abstract void onExit(Creature character);
	
	public void onDieInside(Creature character)
	{
	}
	
	public void onReviveInside(Creature character)
	{
	}
	
	public void onPlayerLoginInside(PlayerInstance player)
	{
	}
	
	public void onPlayerLogoutInside(PlayerInstance player)
	{
	}
	
	public Map<Integer, Creature> getCharacters()
	{
		return _characterList;
	}
	
	public Collection<Creature> getCharactersInside()
	{
		return _characterList.values();
	}
	
	public List<PlayerInstance> getPlayersInside()
	{
		List<PlayerInstance> players = new ArrayList<>();
		for (Creature ch : _characterList.values())
		{
			if ((ch != null) && ch.isPlayer())
			{
				players.add(ch.getActingPlayer());
			}
		}
		
		return players;
	}
	
	/**
	 * Broadcasts packet to all players inside the zone
	 * @param packet
	 */
	public void broadcastPacket(IClientOutgoingPacket packet)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (Creature character : _characterList.values())
		{
			if ((character != null) && character.isPlayer())
			{
				character.sendPacket(packet);
			}
		}
	}
	
	public InstanceType getTargetType()
	{
		return _target;
	}
	
	public void setTargetType(InstanceType type)
	{
		_target = type;
		_checkAffected = true;
	}
	
	public boolean getAllowStore()
	{
		return _allowStore;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _id + "]";
	}
	
	public void visualizeZone(int z)
	{
		getZone().visualizeZone(z);
	}
	
	public void setEnabled(boolean state)
	{
		_enabled = state;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void oustAllPlayers()
	{
		//@formatter:off
		_characterList.values().stream()
			.filter(Objects::nonNull)
			.filter(WorldObject::isPlayer)
			.map(WorldObject::getActingPlayer)
			.filter(PlayerInstance::isOnline)
			.forEach(player -> player.teleToLocation(TeleportWhereType.TOWN));
		//@formatter:off
	}

	/**
	 * @param loc
	 */
	public void movePlayersTo(Location loc)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (Creature character : getCharactersInside())
		{
			if ((character != null) && character.isPlayer())
			{
				PlayerInstance player = character.getActingPlayer();
				if (player.isOnline())
				{
					player.teleToLocation(loc);
				}
			}
		}
	}
}