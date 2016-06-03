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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.handler.ActionHandler;
import org.l2junity.gameserver.handler.ActionShiftHandler;
import org.l2junity.gameserver.handler.IActionHandler;
import org.l2junity.gameserver.handler.IActionShiftHandler;
import org.l2junity.gameserver.idfactory.IdFactory;
import org.l2junity.gameserver.instancemanager.InstanceManager;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.poly.ObjectPoly;
import org.l2junity.gameserver.model.events.ListenersContainer;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.interfaces.IDecayable;
import org.l2junity.gameserver.model.interfaces.IIdentifiable;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.model.interfaces.INamable;
import org.l2junity.gameserver.model.interfaces.IPositionable;
import org.l2junity.gameserver.model.interfaces.ISpawnable;
import org.l2junity.gameserver.model.interfaces.IUniqueId;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.DeleteObject;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;

/**
 * Base class for all interactive objects.
 */
public abstract class WorldObject extends ListenersContainer implements IIdentifiable, INamable, ISpawnable, IUniqueId, IDecayable, IPositionable
{
	/** Name */
	private String _name;
	/** Object ID */
	private int _objectId;
	/** World Region */
	private WorldRegion _worldRegion;
	/** Instance type */
	private InstanceType _instanceType = null;
	private volatile Map<String, Object> _scripts;
	/** X coordinate */
	private final AtomicInteger _x = new AtomicInteger(0);
	/** Y coordinate */
	private final AtomicInteger _y = new AtomicInteger(0);
	/** Z coordinate */
	private final AtomicInteger _z = new AtomicInteger(0);
	/** Orientation */
	private final AtomicInteger _heading = new AtomicInteger(0);
	/** Instance id of object. 0 - Global */
	private Instance _instance = null;
	private boolean _isSpawned;
	private boolean _isInvisible;
	private boolean _isTargetable = true;
	
	public WorldObject(int objectId)
	{
		setInstanceType(InstanceType.L2Object);
		_objectId = objectId;
	}
	
	/**
	 * Gets the instance type of object.
	 * @return the instance type
	 */
	public final InstanceType getInstanceType()
	{
		return _instanceType;
	}
	
	/**
	 * Sets the instance type.
	 * @param newInstanceType the instance type to set
	 */
	protected final void setInstanceType(InstanceType newInstanceType)
	{
		_instanceType = newInstanceType;
	}
	
	/**
	 * Verifies if object is of any given instance types.
	 * @param instanceTypes the instance types to verify
	 * @return {@code true} if object is of any given instance types, {@code false} otherwise
	 */
	public final boolean isInstanceTypes(InstanceType... instanceTypes)
	{
		return _instanceType.isTypes(instanceTypes);
	}
	
	public final void onAction(PlayerInstance player)
	{
		onAction(player, true);
	}
	
	public void onAction(PlayerInstance player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, interact);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(PlayerInstance player)
	{
		IActionShiftHandler handler = ActionShiftHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, true);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onSpawn()
	{
	}
	
	@Override
	public boolean decayMe()
	{
		final WorldRegion reg = getWorldRegion();
		synchronized (this)
		{
			_isSpawned = false;
			setWorldRegion(null);
		}
		
		World.getInstance().removeVisibleObject(this, reg);
		World.getInstance().removeObject(this);
		return true;
	}
	
	public void refreshID()
	{
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	@Override
	public final boolean spawnMe()
	{
		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isSpawned = true;
			setWorldRegion(World.getInstance().getRegion(getLocation()));
			
			// Add the L2Object spawn in the _allobjects of L2World
			World.getInstance().storeObject(this);
			
			// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			getWorldRegion().addVisibleObject(this);
		}
		
		// this can synchronize on others instances, so it's out of synchronized, to avoid deadlocks
		// Add the L2Object spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, getWorldRegion());
		
		onSpawn();
		
		return true;
	}
	
	public final void spawnMe(int x, int y, int z)
	{
		synchronized (this)
		{
			if (x > World.MAP_MAX_X)
			{
				x = World.MAP_MAX_X - 5000;
			}
			if (x < World.MAP_MIN_X)
			{
				x = World.MAP_MIN_X + 5000;
			}
			if (y > World.MAP_MAX_Y)
			{
				y = World.MAP_MAX_Y - 5000;
			}
			if (y < World.MAP_MIN_Y)
			{
				y = World.MAP_MIN_Y + 5000;
			}
			if (z > World.MAP_MAX_Z)
			{
				z = World.MAP_MAX_Z - 1000;
			}
			if (z < World.MAP_MIN_Z)
			{
				z = World.MAP_MIN_Z + 1000;
			}
			
			// Set the x,y,z position of the WorldObject. If flagged with _isSpawned, setXYZ will automatically update world region, so avoid that.
			setXYZ(x, y, z);
		}
		
		// Spawn and update its _worldregion
		spawnMe();
	}
	
	/**
	 * Verify if object can be attacked.
	 * @return {@code true} if object can be attacked, {@code false} otherwise
	 */
	public boolean canBeAttacked()
	{
		return false;
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);
	
	public final boolean isSpawned()
	{
		return getWorldRegion() != null;
	}
	
	public final void setSpawned(boolean value)
	{
		_isSpawned = value;
		if (!_isSpawned)
		{
			setWorldRegion(null);
		}
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	@Override
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final ObjectPoly getPoly()
	{
		final ObjectPoly poly = getScript(ObjectPoly.class);
		return (poly == null) ? addScript(new ObjectPoly(this)) : poly;
	}
	
	public abstract void sendInfo(PlayerInstance activeChar);
	
	public void sendPacket(IClientOutgoingPacket... packets)
	{
	}
	
	public void sendPacket(SystemMessageId id)
	{
	}
	
	public PlayerInstance getActingPlayer()
	{
		return null;
	}
	
	/**
	 * Verify if object is instance of L2Attackable.
	 * @return {@code true} if object is instance of L2Attackable, {@code false} otherwise
	 */
	public boolean isAttackable()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Character.
	 * @return {@code true} if object is instance of L2Character, {@code false} otherwise
	 */
	public boolean isCreature()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2DoorInstance.
	 * @return {@code true} if object is instance of L2DoorInstance, {@code false} otherwise
	 */
	public boolean isDoor()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2MonsterInstance.
	 * @return {@code true} if object is instance of L2MonsterInstance, {@code false} otherwise
	 */
	public boolean isMonster()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Npc.
	 * @return {@code true} if object is instance of L2Npc, {@code false} otherwise
	 */
	public boolean isNpc()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2PetInstance.
	 * @return {@code true} if object is instance of L2PetInstance, {@code false} otherwise
	 */
	public boolean isPet()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2PcInstance.
	 * @return {@code true} if object is instance of L2PcInstance, {@code false} otherwise
	 */
	public boolean isPlayer()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Playable.
	 * @return {@code true} if object is instance of L2Playable, {@code false} otherwise
	 */
	public boolean isPlayable()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2ServitorInstance.
	 * @return {@code true} if object is instance of L2ServitorInstance, {@code false} otherwise
	 */
	public boolean isServitor()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Summon.
	 * @return {@code true} if object is instance of L2Summon, {@code false} otherwise
	 */
	public boolean isSummon()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2TrapInstance.
	 * @return {@code true} if object is instance of L2TrapInstance, {@code false} otherwise
	 */
	public boolean isTrap()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2ItemInstance.
	 * @return {@code true} if object is instance of L2ItemInstance, {@code false} otherwise
	 */
	public boolean isItem()
	{
		return false;
	}
	
	/**
	 * Verifies if the object is a walker NPC.
	 * @return {@code true} if object is a walker NPC, {@code false} otherwise
	 */
	public boolean isWalker()
	{
		return false;
	}
	
	/**
	 * Verifies if this object is a vehicle.
	 * @return {@code true} if object is Vehicle, {@code false} otherwise
	 */
	public boolean isVehicle()
	{
		return false;
	}
	
	public void setTargetable(boolean targetable)
	{
		if (_isTargetable != targetable)
		{
			_isTargetable = targetable;
			if (!targetable)
			{
				World.getInstance().getVisibleObjects(this, Creature.class, creature -> this == creature.getTarget()).forEach(creature ->
				{
					creature.setTarget(null);
					creature.abortAttack();
					creature.abortCast();
				});
			}
		}
	}
	
	/**
	 * @return {@code true} if the object can be targetted by other players, {@code false} otherwise.
	 */
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	/**
	 * Check if the object is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {@code true} if the object is in that zone Id
	 */
	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}
	
	/**
	 * Check if current object has charged shot.
	 * @param type of the shot to be checked.
	 * @return {@code true} if the object has charged shot
	 */
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	/**
	 * Charging shot into the current object.
	 * @param type of the shot to be charged.
	 * @param charged
	 */
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	/**
	 * Try to recharge a shot.
	 * @param physical skill are using Soul shots.
	 * @param magical skill are using Spirit shots.
	 * @param fish
	 */
	public void rechargeShots(boolean physical, boolean magical, boolean fish)
	{
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	public final <T> T addScript(T script)
	{
		if (_scripts == null)
		{
			// Double-checked locking
			synchronized (this)
			{
				if (_scripts == null)
				{
					_scripts = new ConcurrentHashMap<>();
				}
			}
		}
		_scripts.put(script.getClass().getName(), script);
		return script;
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T removeScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.remove(script.getName());
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.get(script.getName());
	}
	
	public void removeStatusListener(Creature object)
	{
		
	}
	
	protected void badCoords()
	{
		if (isCreature())
		{
			decayMe();
		}
		else if (isPlayer())
		{
			((Creature) this).teleToLocation(new Location(0, 0, 0), false);
			((Creature) this).sendMessage("Error with your coords, Please ask a GM for help!");
		}
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		if (x > World.MAP_MAX_X)
		{
			x = World.MAP_MAX_X - 5000;
		}
		if (x < World.MAP_MIN_X)
		{
			x = World.MAP_MIN_X + 5000;
		}
		if (y > World.MAP_MAX_Y)
		{
			y = World.MAP_MAX_Y - 5000;
		}
		if (y < World.MAP_MIN_Y)
		{
			y = World.MAP_MIN_Y + 5000;
		}
		
		setXYZ(x, y, z);
		setSpawned(false);
	}
	
	public final void setLocationInvisible(ILocational loc)
	{
		setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final WorldRegion getWorldRegion()
	{
		return _worldRegion;
	}
	
	public void setWorldRegion(WorldRegion value)
	{
		_worldRegion = value;
	}
	
	/**
	 * Gets the X coordinate.
	 * @return the X coordinate
	 */
	@Override
	public int getX()
	{
		return _x.get();
	}
	
	/**
	 * Gets the Y coordinate.
	 * @return the Y coordinate
	 */
	@Override
	public int getY()
	{
		return _y.get();
	}
	
	/**
	 * Gets the Z coordinate.
	 * @return the Z coordinate
	 */
	@Override
	public int getZ()
	{
		return _z.get();
	}
	
	/**
	 * Gets the heading.
	 * @return the heading
	 */
	@Override
	public int getHeading()
	{
		return _heading.get();
	}
	
	/**
	 * Gets the instance ID.
	 * @return the instance ID
	 */
	public int getInstanceId()
	{
		final Instance instance = _instance;
		return (instance != null) ? instance.getId() : 0;
	}
	
	/**
	 * Check if object is inside instance world.
	 * @return {@code true} when object is inside any instance world, otherwise {@code false}
	 */
	public boolean isInInstance()
	{
		return _instance != null;
	}
	
	/**
	 * Get instance world where object is currently located.
	 * @return {@link Instance} if object is inside instance world, otherwise {@code null}
	 */
	public Instance getInstanceWorld()
	{
		return _instance;
	}
	
	/**
	 * Gets the location object.
	 * @return the location object
	 */
	@Override
	public Location getLocation()
	{
		return new Location(getX(), getY(), getZ(), getHeading());
	}
	
	/**
	 * Sets the X coordinate
	 * @param newX the X coordinate
	 */
	@Override
	public void setX(int newX)
	{
		_x.set(newX);
	}
	
	/**
	 * Sets the Y coordinate
	 * @param newY the Y coordinate
	 */
	@Override
	public void setY(int newY)
	{
		_y.set(newY);
	}
	
	/**
	 * Sets the Z coordinate
	 * @param newZ the Z coordinate
	 */
	@Override
	public void setZ(int newZ)
	{
		_z.set(newZ);
	}
	
	/**
	 * Sets the x, y, z coordinate.
	 * @param newX the X coordinate
	 * @param newY the Y coordinate
	 * @param newZ the Z coordinate
	 */
	@Override
	public void setXYZ(int newX, int newY, int newZ)
	{
		setX(newX);
		setY(newY);
		setZ(newZ);
		
		try
		{
			if (_isSpawned)
			{
				final WorldRegion oldRegion = getWorldRegion();
				final WorldRegion newRegion = World.getInstance().getRegion(this);
				if (newRegion != oldRegion)
				{
					if (oldRegion != null)
					{
						oldRegion.removeVisibleObject(this);
					}
					newRegion.addVisibleObject(this);
					World.getInstance().switchRegion(this, newRegion);
					setWorldRegion(newRegion);
				}
			}
		}
		catch (Exception e)
		{
			badCoords();
		}
	}
	
	/**
	 * Sets the x, y, z coordinate.
	 * @param loc the location object
	 */
	@Override
	public void setXYZ(ILocational loc)
	{
		setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Sets heading of object.
	 * @param newHeading the new heading
	 */
	@Override
	public void setHeading(int newHeading)
	{
		_heading.set(newHeading);
	}
	
	/**
	 * Sets instance for current object by instance ID.<br>
	 * @param id ID of instance world which should be set (0 means normal world)
	 */
	public void setInstanceById(int id)
	{
		final Instance instance = InstanceManager.getInstance().getInstance(id);
		if ((id != 0) && (instance == null))
		{
			return;
		}
		setInstance(instance);
	}
	
	/**
	 * Sets instance where current object belongs.
	 * @param newInstance new instance world for object
	 */
	public synchronized void setInstance(Instance newInstance)
	{
		// Check if new and old instances are identical
		if (_instance == newInstance)
		{
			return;
		}
		
		// Leave old instance
		if (_instance != null)
		{
			_instance.onInstanceChange(this, false);
		}
		
		// Set new instance
		_instance = newInstance;
		
		// Enter into new instance
		if (newInstance != null)
		{
			newInstance.onInstanceChange(this, true);
		}
	}
	
	/**
	 * Sets location of object.
	 * @param loc the location object
	 */
	@Override
	public void setLocation(Location loc)
	{
		_x.set(loc.getX());
		_y.set(loc.getY());
		_z.set(loc.getZ());
		_heading.set(loc.getHeading());
	}
	
	/**
	 * Calculates distance between this L2Object and given x, y , z.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 * @param includeZAxis if {@code true} Z axis will be included
	 * @param squared if {@code true} return will be squared
	 * @return distance between object and given x, y, z.
	 */
	public final double calculateDistance(int x, int y, int z, boolean includeZAxis, boolean squared)
	{
		final double distance = Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2) + (includeZAxis ? Math.pow(z - getZ(), 2) : 0);
		return (squared) ? distance : Math.sqrt(distance);
	}
	
	/**
	 * Calculates distance between this L2Object and given location.
	 * @param loc the location object
	 * @param includeZAxis if {@code true} Z axis will be included
	 * @param squared if {@code true} return will be squared
	 * @return distance between object and given location.
	 */
	public final double calculateDistance(ILocational loc, boolean includeZAxis, boolean squared)
	{
		return calculateDistance(loc.getX(), loc.getY(), loc.getZ(), includeZAxis, squared);
	}
	
	/**
	 * Calculates the angle in degrees from this object to the given object.<br>
	 * The return value can be described as how much this object has to turn<br>
	 * to have the given object directly in front of it.
	 * @param target the object to which to calculate the angle
	 * @return the angle this object has to turn to have the given object in front of it
	 */
	public final double calculateDirectionTo(ILocational target)
	{
		int heading = Util.calculateHeadingFrom(this, target) - getHeading();
		if (heading < 0)
		{
			heading = 65535 + heading;
		}
		return Util.convertHeadingToDegree(heading);
	}
	
	/**
	 * @return {@code true} if this object is invisible, {@code false} otherwise.
	 */
	public boolean isInvisible()
	{
		return _isInvisible;
	}
	
	/**
	 * Sets this object as invisible or not
	 * @param invis
	 */
	public void setInvisible(boolean invis)
	{
		_isInvisible = invis;
		if (invis)
		{
			final DeleteObject deletePacket = new DeleteObject(this);
			World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
			{
				if (!isVisibleFor(player))
				{
					player.sendPacket(deletePacket);
				}
			});
		}
		
		// Broadcast information regarding the object to those which are suppose to see.
		broadcastInfo();
	}
	
	/**
	 * @param player
	 * @return {@code true} if player can see an invisible object if it's invisible, {@code false} otherwise.
	 */
	public boolean isVisibleFor(PlayerInstance player)
	{
		return !isInvisible() || player.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS);
	}
	
	/**
	 * Broadcasts describing info to known players.
	 */
	public void broadcastInfo()
	{
		World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
		{
			if (isVisibleFor(player))
			{
				sendInfo(player);
			}
		});
	}
	
	public boolean isInvul()
	{
		return false;
	}
	
	public boolean isInSurroundingRegion(WorldObject worldObject)
	{
		if (worldObject == null)
		{
			return false;
		}
		
		final WorldRegion worldRegion1 = worldObject.getWorldRegion();
		if (worldRegion1 == null)
		{
			return false;
		}
		
		final WorldRegion worldRegion2 = getWorldRegion();
		if (worldRegion2 == null)
		{
			return false;
		}
		
		return worldRegion1.isSurroundingRegion(worldRegion2);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return ((obj instanceof WorldObject) && (((WorldObject) obj).getObjectId() == getObjectId()));
	}
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
}
