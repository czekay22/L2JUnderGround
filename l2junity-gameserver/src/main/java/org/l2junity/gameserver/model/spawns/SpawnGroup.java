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
package org.l2junity.gameserver.model.spawns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.interfaces.ITerritorized;
import org.l2junity.gameserver.model.zone.type.BannedSpawnTerritory;
import org.l2junity.gameserver.model.zone.type.SpawnTerritory;

/**
 * @author UnAfraid
 */
public class SpawnGroup implements Cloneable, ITerritorized
{
	private final String _name;
	private final boolean _spawnByDefault;
	private List<SpawnTerritory> _territories;
	private List<BannedSpawnTerritory> _bannedTerritories;
	private final List<NpcSpawnTemplate> _spawns = new ArrayList<>();
	
	public SpawnGroup(StatsSet set)
	{
		this(set.getString("name", null), set.getBoolean("spawnByDefault", true));
	}
	
	private SpawnGroup(String name, boolean spawnByDefault)
	{
		_name = name;
		_spawnByDefault = spawnByDefault;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isSpawningByDefault()
	{
		return _spawnByDefault;
	}
	
	public void addSpawn(NpcSpawnTemplate template)
	{
		_spawns.add(template);
	}
	
	public List<NpcSpawnTemplate> getSpawns()
	{
		return _spawns;
	}
	
	@Override
	public void addTerritory(SpawnTerritory territory)
	{
		if (_territories == null)
		{
			_territories = new ArrayList<>();
		}
		_territories.add(territory);
	}
	
	@Override
	public List<SpawnTerritory> getTerritories()
	{
		return _territories != null ? _territories : Collections.emptyList();
	}
	
	@Override
	public void addBannedTerritory(BannedSpawnTerritory territory)
	{
		if (_bannedTerritories == null)
		{
			_bannedTerritories = new ArrayList<>();
		}
		_bannedTerritories.add(territory);
	}
	
	@Override
	public List<BannedSpawnTerritory> getBannedTerritories()
	{
		return _bannedTerritories != null ? _bannedTerritories : Collections.emptyList();
	}
	
	public List<NpcSpawnTemplate> getSpawnsById(int id)
	{
		return _spawns.stream().filter(spawn -> spawn.getId() == id).collect(Collectors.toList());
	}
	
	public void spawnAll(Instance instance)
	{
		_spawns.forEach(template -> template.spawn(instance));
	}
	
	public void despawnAll()
	{
		_spawns.forEach(NpcSpawnTemplate::despawn);
	}
	
	@Override
	public SpawnGroup clone()
	{
		final SpawnGroup group = new SpawnGroup(_name, _spawnByDefault);
		
		// Clone banned territories
		for (BannedSpawnTerritory territory : getBannedTerritories())
		{
			group.addBannedTerritory(territory);
		}
		
		// Clone territories
		for (SpawnTerritory territory : getTerritories())
		{
			group.addTerritory(territory);
		}
		
		// Clone spawns
		for (NpcSpawnTemplate spawn : _spawns)
		{
			group.addSpawn(spawn.clone());
		}
		
		return group;
	}
}
