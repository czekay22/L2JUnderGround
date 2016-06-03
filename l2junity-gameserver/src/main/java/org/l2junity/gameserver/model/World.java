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
package org.l2junity.gameserver.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.l2junity.gameserver.ai.CharacterAI;
import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.npc.OnNpcCreatureSee;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.network.client.send.DeleteObject;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class World
{
	private static final Logger _log = LoggerFactory.getLogger(World.class);
	/** Gracia border Flying objects not allowed to the east of it. */
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	
	/** Bit shift, defines number of regions note, shifting by 15 will result in regions corresponding to map tiles shifting by 11 divides one tile to 16x16 regions. */
	public static final int SHIFT_BY = 11;
	public static final int SHIFT_BY_Z = 10;
	
	private static final int TILE_SIZE = 32768;
	
	/** Map dimensions */
	public static final int TILE_X_MIN = 11;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_X_MAX = 28;
	public static final int TILE_Y_MAX = 26;
	public static final int TILE_ZERO_COORD_X = 20;
	public static final int TILE_ZERO_COORD_Y = 18;
	public static final int MAP_MIN_X = (TILE_X_MIN - TILE_ZERO_COORD_X) * TILE_SIZE;
	public static final int MAP_MIN_Y = (TILE_Y_MIN - TILE_ZERO_COORD_Y) * TILE_SIZE;
	public static final int MAP_MIN_Z = -TILE_SIZE / 2;
	
	public static final int MAP_MAX_X = ((TILE_X_MAX - TILE_ZERO_COORD_X) + 1) * TILE_SIZE;
	public static final int MAP_MAX_Y = ((TILE_Y_MAX - TILE_ZERO_COORD_Y) + 1) * TILE_SIZE;
	public static final int MAP_MAX_Z = TILE_SIZE / 2;
	
	/** calculated offset used so top left region is 0,0 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);
	
	/** number of regions */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;
	
	public static final int REGION_MIN_DIMENSION = Math.min(TILE_SIZE / (TILE_SIZE >> SHIFT_BY_Z), TILE_SIZE / (TILE_SIZE >> SHIFT_BY));
	
	/** Map containing all the players in game. */
	private final Map<Integer, PlayerInstance> _allPlayers = new ConcurrentHashMap<>();
	/** Map containing all visible objects. */
	private final Map<Integer, WorldObject> _allObjects = new ConcurrentHashMap<>();
	/** Map used for debug. */
	// private final Map<Integer, String> _allObjectsDebug = new ConcurrentHashMap<>();
	/** Map with the pets instances and their owner ID. */
	private final Map<Integer, L2PetInstance> _petsInstance = new ConcurrentHashMap<>();
	
	private final WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];
	
	/** Constructor of L2World. */
	protected World()
	{
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int z = 0; z <= REGIONS_Z; z++)
				{
					_worldRegions[x][y][z] = new WorldRegion(x, y, z);
				}
			}
		}
		
		_log.info("(" + REGIONS_X + " by " + REGIONS_Y + " by " + REGIONS_Z + ") World Region Grid set up.");
	}
	
	/**
	 * Adds an object to the world.<br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Withdraw an item from the warehouse, create an item</li>
	 * <li>Spawn a L2Character (PC, NPC, Pet)</li>
	 * </ul>
	 * @param object
	 */
	public void storeObject(WorldObject object)
	{
		if (_allObjects.containsKey(object.getObjectId()))
		{
			// _log.warn(getClass().getSimpleName() + ": Current object: " + object + " already exist in OID map!");
			// _log.warn(CommonUtil.getTraceString(Thread.currentThread().getStackTrace()));
			// _log.warn(getClass().getSimpleName() + ": Previous object: " + _allObjects.get(object.getObjectId()) + " already exist in OID map!");
			// _log.warn(_allObjectsDebug.get(object.getObjectId()));
			// _log.warn("---------------------- End ---------------------");
			return;
		}
		
		_allObjects.put(object.getObjectId(), object);
		// _allObjectsDebug.put(object.getObjectId(), CommonUtil.getTraceString(Thread.currentThread().getStackTrace()));
	}
	
	/**
	 * Removes an object from the world.<br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Delete item from inventory, transfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li>
	 * </ul>
	 * @param object the object to remove
	 */
	public void removeObject(WorldObject object)
	{
		_allObjects.remove(object.getObjectId());
		// _allObjectsDebug.remove(object.getObjectId());
	}
	
	/**
	 * <B><U> Example of use</U>:</B>
	 * <ul>
	 * <li>Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li>
	 * </ul>
	 * @param objectId Identifier of the L2Object
	 * @return the L2Object object that belongs to an ID or null if no object found.
	 */
	public WorldObject findObject(int objectId)
	{
		return _allObjects.get(objectId);
	}
	
	public Collection<WorldObject> getVisibleObjects()
	{
		return _allObjects.values();
	}
	
	/**
	 * Get the count of all visible objects in world.
	 * @return count off all L2World objects
	 */
	public int getVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	public Collection<PlayerInstance> getPlayers()
	{
		return _allPlayers.values();
	}
	
	/**
	 * <B>If you have access to player objectId use {@link #getPlayer(int playerObjId)}</B>
	 * @param name Name of the player to get Instance
	 * @return the player instance corresponding to the given name.
	 */
	public PlayerInstance getPlayer(String name)
	{
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	/**
	 * @param objectId of the player to get Instance
	 * @return the player instance corresponding to the given object ID.
	 */
	public PlayerInstance getPlayer(int objectId)
	{
		return _allPlayers.get(objectId);
	}
	
	/**
	 * @param ownerId ID of the owner
	 * @return the pet instance from the given ownerId.
	 */
	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.
	 * @param ownerId ID of the owner
	 * @param pet L2PetInstance of the pet
	 * @return
	 */
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param pet the pet to remove
	 */
	public void removePet(L2PetInstance pet)
	{
		_petsInstance.remove(pet.getOwner().getObjectId());
	}
	
	/**
	 * Add a L2Object in the world. <B><U> Concept</U> :</B> L2Object (including PlayerInstance) are identified in <B>_visibleObjects</B> of his current WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * PlayerInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <B><U> Actions</U> :</B>
	 * <li>Add the L2Object object in _allPlayers* of L2World</li>
	 * <li>Add the L2Object object in _gmList** of GmListTable</li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding WorldRegion L2Characters</li><BR>
	 * <li>If object is a L2Character, add all surrounding L2Object in its _knownObjects and all surrounding PlayerInstance in its _knownPlayer</li><BR>
	 * <I>* only if object is a PlayerInstance</I><BR>
	 * <I>** only if object is a GM PlayerInstance</I>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of WorldRegion (need synchronisation)</B></FONT><BR> <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects and _allPlayers* of L2World (need
	 * synchronisation)</B></FONT> <B><U> Example of use </U> :</B> <li>Drop an Item</li> <li>Spawn a L2Character</li> <li>Apply Death Penalty of a PlayerInstance</li>
	 * @param object L2object to add in the world
	 * @param newRegion WorldRegion in wich the object will be add (not used)
	 */
	public void addVisibleObject(WorldObject object, WorldRegion newRegion)
	{
		// TODO: this code should be obsoleted by protection in putObject func...
		if (object.isPlayer())
		{
			PlayerInstance player = object.getActingPlayer();
			if (!player.isTeleporting())
			{
				final PlayerInstance old = getPlayer(player.getObjectId());
				if (old != null)
				{
					_log.warn("Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.logout();
					old.logout();
					return;
				}
				addPlayerToWorld(player);
			}
		}
		
		if (!newRegion.isActive())
		{
			return;
		}
		
		forEachVisibleObject(object, WorldObject.class, 1, wo ->
		{
			if (object.isPlayer() && wo.isVisibleFor((PlayerInstance) object))
			{
				wo.sendInfo((PlayerInstance) object);
				if (wo.isCreature())
				{
					final CharacterAI ai = ((Creature) wo).getAI();
					if (ai != null)
					{
						ai.describeStateToPlayer((PlayerInstance) object);
						if (wo.isMonster())
						{
							if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
							{
								ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
							}
						}
					}
				}
			}
			
			if (wo.isPlayer() && object.isVisibleFor((PlayerInstance) wo))
			{
				object.sendInfo((PlayerInstance) wo);
				if (object.isCreature())
				{
					final CharacterAI ai = ((Creature) object).getAI();
					if (ai != null)
					{
						ai.describeStateToPlayer((PlayerInstance) wo);
						if (object.isMonster())
						{
							if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
							{
								ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
							}
						}
					}
				}
			}
			
			if (wo.isNpc() && object.isCreature())
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnNpcCreatureSee((Npc) wo, (Creature) object, object.isSummon()), (Npc) wo);
			}
			
			if (object.isNpc() && wo.isCreature())
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnNpcCreatureSee((Npc) object, (Creature) wo, wo.isSummon()), (Npc) object);
			}
		});
	}
	
	/**
	 * Adds the player to the world.
	 * @param player the player to add
	 */
	public void addPlayerToWorld(PlayerInstance player)
	{
		_allPlayers.put(player.getObjectId(), player);
	}
	
	/**
	 * Remove the player from the world.
	 * @param player the player to remove
	 */
	public void removeFromAllPlayers(PlayerInstance player)
	{
		_allPlayers.remove(player.getObjectId());
	}
	
	/**
	 * Remove a L2Object from the world. <B><U> Concept</U> :</B> L2Object (including PlayerInstance) are identified in <B>_visibleObjects</B> of his current WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * PlayerInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <B><U> Actions</U> :</B>
	 * <li>Remove the L2Object object from _allPlayers* of L2World</li>
	 * <li>Remove the L2Object object from _visibleObjects and _allPlayers* of WorldRegion</li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding WorldRegion L2Characters</li><BR>
	 * <li>If object is a L2Character, remove all L2Object from its _knownObjects and all PlayerInstance from its _knownPlayer</li>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World</B></FONT> <I>* only if object is a PlayerInstance</I><BR> <I>** only if object is a GM PlayerInstance</I> <B><U> Example of use </U> :</B> <li>Pickup an Item</li> <li>Decay a
	 * L2Character</li>
	 * @param object L2object to remove from the world
	 * @param oldRegion WorldRegion in which the object was before removing
	 */
	public void removeVisibleObject(WorldObject object, WorldRegion oldRegion)
	{
		if (object == null)
		{
			return;
		}
		
		if (oldRegion != null)
		{
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding WorldRegion Creatures
			oldRegion.forEachSurroundingRegion(w ->
			{
				for (WorldObject wo : w.getVisibleObjects().values())
				{
					if (wo == object)
					{
						continue;
					}
					
					if (object.isCreature())
					{
						final Creature objectCreature = (Creature) object;
						final CharacterAI ai = objectCreature.getAI();
						if (ai != null)
						{
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, wo);
						}
						
						if (objectCreature.getTarget() == wo)
						{
							objectCreature.setTarget(null);
						}
						
						if (object.isPlayer())
						{
							object.sendPacket(new DeleteObject(wo));
						}
					}
					
					if (wo.isCreature())
					{
						final Creature woCreature = (Creature) wo;
						final CharacterAI ai = woCreature.getAI();
						if (ai != null)
						{
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
						}
						
						if (woCreature.getTarget() == object)
						{
							woCreature.setTarget(null);
						}
						
						if (wo.isPlayer())
						{
							wo.sendPacket(new DeleteObject(object));
						}
					}
				}
				return true;
			});
			
			if (object.isPlayer())
			{
				final PlayerInstance player = object.getActingPlayer();
				if (!player.isTeleporting())
				{
					removeFromAllPlayers(player);
				}
			}
		}
	}
	
	public void switchRegion(WorldObject object, WorldRegion newRegion)
	{
		final WorldRegion oldRegion = object.getWorldRegion();
		if ((oldRegion == null) || (oldRegion == newRegion))
		{
			return;
		}
		
		oldRegion.forEachSurroundingRegion(w ->
		{
			if (!newRegion.isSurroundingRegion(w))
			{
				for (WorldObject wo : w.getVisibleObjects().values())
				{
					if (wo == object)
					{
						continue;
					}
					
					if (object.isCreature())
					{
						final Creature objectCreature = (Creature) object;
						final CharacterAI ai = objectCreature.getAI();
						if (ai != null)
						{
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, wo);
						}
						
						if (objectCreature.getTarget() == wo)
						{
							objectCreature.setTarget(null);
						}
						
						if (object.isPlayer())
						{
							object.sendPacket(new DeleteObject(wo));
						}
					}
					
					if (wo.isCreature())
					{
						final Creature woCreature = (Creature) wo;
						final CharacterAI ai = woCreature.getAI();
						if (ai != null)
						{
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
						}
						
						if (woCreature.getTarget() == object)
						{
							woCreature.setTarget(null);
						}
						
						if (wo.isPlayer())
						{
							wo.sendPacket(new DeleteObject(object));
						}
					}
				}
			}
			return true;
		});
		
		newRegion.forEachSurroundingRegion(w ->
		{
			if (!oldRegion.isSurroundingRegion(w))
			{
				for (WorldObject wo : w.getVisibleObjects().values())
				{
					if ((wo == object) || (wo.getInstanceWorld() != object.getInstanceWorld()))
					{
						continue;
					}
					
					if (object.isPlayer() && wo.isVisibleFor((PlayerInstance) object))
					{
						wo.sendInfo((PlayerInstance) object);
						if (wo.isCreature())
						{
							final CharacterAI ai = ((Creature) wo).getAI();
							if (ai != null)
							{
								ai.describeStateToPlayer((PlayerInstance) object);
								if (wo.isMonster())
								{
									if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
									{
										ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									}
								}
							}
						}
					}
					
					if (wo.isPlayer() && object.isVisibleFor((PlayerInstance) wo))
					{
						object.sendInfo((PlayerInstance) wo);
						if (object.isCreature())
						{
							final CharacterAI ai = ((Creature) object).getAI();
							if (ai != null)
							{
								ai.describeStateToPlayer((PlayerInstance) wo);
								if (object.isMonster())
								{
									if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
									{
										ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									}
								}
							}
						}
					}
					
					if (wo.isNpc() && object.isCreature())
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcCreatureSee((Npc) wo, (Creature) object, object.isSummon()), (Npc) wo);
					}
					
					if (object.isNpc() && wo.isCreature())
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcCreatureSee((Npc) object, (Creature) wo, wo.isSummon()), (Npc) object);
					}
				}
			}
			return true;
		});
	}
	
	private <T extends WorldObject> void forEachVisibleObject(WorldObject object, Class<T> clazz, int depth, Consumer<T> c)
	{
		if (object == null)
		{
			return;
		}
		
		final WorldRegion centerWorldRegion = getRegion(object);
		if (centerWorldRegion == null)
		{
			return;
		}
		
		for (int x = Math.max(centerWorldRegion.getRegionX() - depth, 0); x <= Math.min(centerWorldRegion.getRegionX() + depth, REGIONS_X); x++)
		{
			for (int y = Math.max(centerWorldRegion.getRegionY() - depth, 0); y <= Math.min(centerWorldRegion.getRegionY() + depth, REGIONS_Y); y++)
			{
				for (int z = Math.max(centerWorldRegion.getRegionZ() - depth, 0); z <= Math.min(centerWorldRegion.getRegionZ() + depth, REGIONS_Z); z++)
				{
					for (WorldObject visibleObject : _worldRegions[x][y][z].getVisibleObjects().values())
					{
						if ((visibleObject == null) || (visibleObject == object) || !clazz.isInstance(visibleObject))
						{
							continue;
						}
						
						if (visibleObject.getInstanceWorld() != object.getInstanceWorld())
						{
							continue;
						}
						
						c.accept(clazz.cast(visibleObject));
					}
				}
			}
		}
	}
	
	public <T extends WorldObject> void forEachVisibleObject(WorldObject object, Class<T> clazz, Consumer<T> c)
	{
		forEachVisibleObject(object, clazz, 1, c);
	}
	
	public <T extends WorldObject> void forEachVisibleObjectInRange(WorldObject object, Class<T> clazz, int range, Consumer<T> c)
	{
		if (object == null)
		{
			return;
		}
		
		final WorldRegion centerWorldRegion = getRegion(object);
		if (centerWorldRegion == null)
		{
			return;
		}
		
		final int depth = (range / REGION_MIN_DIMENSION) + 1;
		for (int x = Math.max(centerWorldRegion.getRegionX() - depth, 0); x <= Math.min(centerWorldRegion.getRegionX() + depth, REGIONS_X); x++)
		{
			for (int y = Math.max(centerWorldRegion.getRegionY() - depth, 0); y <= Math.min(centerWorldRegion.getRegionY() + depth, REGIONS_Y); y++)
			{
				for (int z = Math.max(centerWorldRegion.getRegionZ() - depth, 0); z <= Math.min(centerWorldRegion.getRegionZ() + depth, REGIONS_Z); z++)
				{
					final int x1 = (x - OFFSET_X) << SHIFT_BY;
					final int y1 = (y - OFFSET_Y) << SHIFT_BY;
					final int z1 = (z - OFFSET_Z) << SHIFT_BY_Z;
					final int x2 = ((x + 1) - OFFSET_X) << SHIFT_BY;
					final int y2 = ((y + 1) - OFFSET_Y) << SHIFT_BY;
					final int z2 = ((z + 1) - OFFSET_Z) << SHIFT_BY_Z;
					if (Util.cubeIntersectsSphere(x1, y1, z1, x2, y2, z2, object.getX(), object.getY(), object.getZ(), range))
					{
						for (WorldObject visibleObject : _worldRegions[x][y][z].getVisibleObjects().values())
						{
							if ((visibleObject == null) || (visibleObject == object) || !clazz.isInstance(visibleObject))
							{
								continue;
							}
							
							if (visibleObject.getInstanceWorld() != object.getInstanceWorld())
							{
								continue;
							}
							
							if (visibleObject.calculateDistance(object, true, false) <= range)
							{
								c.accept(clazz.cast(visibleObject));
							}
						}
					}
				}
			}
		}
	}
	
	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObject(object, clazz, result::add);
		return result;
	}
	
	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz, Predicate<T> predicate)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObject(object, clazz, o ->
		{
			if (predicate.test(o))
			{
				result.add(o);
			}
		});
		return result;
	}
	
	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz, int range)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObjectInRange(object, clazz, range, result::add);
		return result;
	}
	
	public <T extends WorldObject> List<T> getVisibleObjects(WorldObject object, Class<T> clazz, int range, Predicate<T> predicate)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObjectInRange(object, clazz, range, o ->
		{
			if (predicate.test(o))
			{
				result.add(o);
			}
		});
		return result;
	}
	
	/**
	 * Calculate the current WorldRegions of the object according to its position (x,y). <B><U> Example of use </U> :</B>
	 * <li>Set position of a new L2Object (drop, spawn...)</li>
	 * <li>Update position of a L2Object after a movement</li><BR>
	 * @param point position of the object
	 * @return
	 */
	public WorldRegion getRegion(ILocational point)
	{
		return getRegion(point.getX(), point.getY(), point.getZ());
	}
	
	public WorldRegion getRegion(int x, int y, int z)
	{
		try
		{
			return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y][(z >> SHIFT_BY_Z) + OFFSET_Z];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			_log.warn("Incorrect world region X: {} Y: {} Z: {} for coordinates x: {} y: {} z: {}", ((x >> SHIFT_BY) + OFFSET_X), ((y >> SHIFT_BY) + OFFSET_Y), ((z >> SHIFT_BY_Z) + OFFSET_Z), x, y, z);
			return null;
		}
	}
	
	/**
	 * Returns the whole 3d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 * @return
	 */
	public WorldRegion[][][] getWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * Check if the current WorldRegions of the object is valid according to its position (x,y). <B><U> Example of use </U> :</B>
	 * <li>Init WorldRegions</li><BR>
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @param z Z position of the object
	 * @return True if the WorldRegion is valid
	 */
	public static boolean validRegion(int x, int y, int z)
	{
		return ((x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y)) && (z >= 0) && (z <= REGIONS_Z);
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int z = 0; z <= REGIONS_Z; z++)
				{
					_worldRegions[x][y][z].deleteVisibleNpcSpawns();
				}
			}
		}
		_log.info("All visible NPC's deleted.");
	}
	
	/**
	 * @return the current instance of L2World
	 */
	public static World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final World _instance = new World();
	}
}
