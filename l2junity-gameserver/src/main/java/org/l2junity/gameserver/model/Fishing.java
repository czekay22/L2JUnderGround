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

import java.util.concurrent.ScheduledFuture;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.data.xml.impl.FishingData;
import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerFishing;
import org.l2junity.gameserver.model.interfaces.ILocational;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.items.type.WeaponType;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.model.zone.type.FishingZone;
import org.l2junity.gameserver.model.zone.type.WaterZone;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.PlaySound;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.fishing.ExFishingEnd;
import org.l2junity.gameserver.network.client.send.fishing.ExFishingEnd.FishingEndReason;
import org.l2junity.gameserver.network.client.send.fishing.ExFishingEnd.FishingEndType;
import org.l2junity.gameserver.network.client.send.fishing.ExFishingStart;
import org.l2junity.gameserver.network.client.send.fishing.ExUserInfoFishing;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bit
 */
public class Fishing
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(Fishing.class);
	private volatile ILocational _baitLocation = new Location(0, 0, 0);
	
	private final PlayerInstance _player;
	private ScheduledFuture<?> _reelInTask;
	private ScheduledFuture<?> _startFishingTask;
	private boolean _isFishing = false;
	
	public Fishing(PlayerInstance player)
	{
		_player = player;
	}
	
	public synchronized boolean isFishing()
	{
		return _isFishing;
	}
	
	public boolean isAtValidLocation()
	{
		// TODO: implement checking direction
		return _player.isInsideZone(ZoneId.FISHING);
	}
	
	public boolean canFish()
	{
		return !_player.isDead() && !_player.isAlikeDead() && !_player.hasBlockActions();
	}
	
	private FishingBaitData getCurrentBaitData()
	{
		ItemInstance bait = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return bait != null ? FishingData.getInstance().getBaitData(bait.getId()) : null;
	}
	
	private void cancelTasks()
	{
		if (_reelInTask != null)
		{
			_reelInTask.cancel(false);
			_reelInTask = null;
		}
		
		if (_startFishingTask != null)
		{
			_startFishingTask.cancel(false);
			_startFishingTask = null;
		}
	}
	
	public synchronized void startFishing()
	{
		if (isFishing())
		{
			return;
		}
		_isFishing = true;
		castLine();
	}
	
	private void castLine()
	{
		if (!Config.ALLOWFISHING && !_player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
		{
			_player.sendMessage("Fishing is disabled.");
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		cancelTasks();
		
		if (!canFish())
		{
			if (isFishing())
			{
				_player.sendPacket(SystemMessageId.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
			}
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		final int minPlayerLevel = FishingData.getInstance().getMinPlayerLevel();
		if (_player.getLevel() < minPlayerLevel)
		{
			if (minPlayerLevel == 85)
			{
				_player.sendPacket(SystemMessageId.FISHING_IS_AVAILABLE_TO_CHARACTERS_LV_85_OR_ABOVE);
			}
			else
			// In case of custom fishing level requirement set in config.
			{
				_player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_FISHING_LEVEL_REQUIREMENTS);
			}
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		final ItemInstance rod = _player.getActiveWeaponInstance();
		if ((rod == null) || (rod.getItemType() != WeaponType.FISHINGROD))
		{
			_player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		final FishingBaitData baitData = getCurrentBaitData();
		if (baitData == null)
		{
			_player.sendPacket(SystemMessageId.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		if (_player.isTransformed() || _player.isInBoat())
		{
			_player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHEN_TRANSFORMED_OR_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT_IT_S_AGAINST_THE_RULES);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		if (_player.isInCraftMode() || _player.isInStoreMode())
		{
			_player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_WORKSHOP_OR_PRIVATE_STORE);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		if (_player.isInsideZone(ZoneId.WATER) || _player.isInWater())
		{
			_player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHILE_UNDER_WATER);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		_baitLocation = calculateBaitLocation();
		if (!isAtValidLocation() || (_baitLocation == null))
		{
			if (isFishing())
			{
				_player.sendPacket(SystemMessageId.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
				_player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				_player.sendPacket(SystemMessageId.YOU_CAN_T_FISH_HERE);
				_player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			stopFishing(FishingEndType.ERROR);
			return;
		}
		
		if (!_player.isChargedShot(ShotType.FISH_SOULSHOTS))
		{
			_player.rechargeShots(false, false, true);
		}
		
		_reelInTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			_player.getFishing().reelInWithReward();
			_startFishingTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> _player.getFishing().castLine(), Rnd.get(FishingData.getInstance().getFishingTimeWaitMin(), FishingData.getInstance().getFishingTimeWaitMax()));
		}, Rnd.get(FishingData.getInstance().getFishingTimeMin(), FishingData.getInstance().getFishingTimeMax()));
		_player.stopMove(null);
		_player.broadcastPacket(new ExFishingStart(_player, -1, baitData.getLevel(), _baitLocation));
		_player.sendPacket(new ExUserInfoFishing(_player, true, _baitLocation));
		_player.sendPacket(new PlaySound("SF_P_01"));
		_player.sendPacket(SystemMessageId.YOU_CAST_YOUR_LINE_AND_START_TO_FISH);
	}
	
	public void reelInWithReward()
	{
		// Fish may or may not eat the hook. If it does - it consumes fishing bait and fishing shot.
		// Then player may or may not catch the fish. Using fishing shots increases chance to win.
		final FishingBaitData baitData = getCurrentBaitData();
		if (baitData == null)
		{
			reelIn(FishingEndReason.LOSE, false);
			LOGGER.warn("Player {} is fishing with unhandled bait: {}", _player, _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND));
			return;
		}
		
		double chance = baitData.getChance();
		if (_player.isChargedShot(ShotType.FISH_SOULSHOTS))
		{
			chance *= 1.25; // +25 % chance to win
			_player.setChargedShot(ShotType.FISH_SOULSHOTS, false);
		}
		
		if (Rnd.get(0, 100) <= chance)
		{
			reelIn(FishingEndReason.WIN, true);
		}
		else
		{
			reelIn(FishingEndReason.LOSE, true);
		}
	}
	
	private void reelIn(FishingEndReason reason, boolean consumeBait)
	{
		if (!isFishing())
		{
			return;
		}
		
		cancelTasks();
		
		try
		{
			final ItemInstance bait = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if (consumeBait)
			{
				if ((bait == null) || !_player.getInventory().updateItemCount(null, bait, -1, _player, null))
				{
					reason = FishingEndReason.LOSE; // no bait - no reward
					return;
				}
			}
			
			if ((reason == FishingEndReason.WIN) && (bait != null))
			{
				FishingBaitData baitData = FishingData.getInstance().getBaitData(bait.getId());
				final int numRewards = baitData.getRewards().size();
				if (numRewards > 0)
				{
					// TODO: verify, totally guessed
					FishingData fishingData = FishingData.getInstance();
					int lvlModifier = _player.getLevel() * _player.getLevel();
					_player.addExpAndSp(Rnd.get(fishingData.getExpRateMin(), fishingData.getExpRateMax()) * lvlModifier, Rnd.get(fishingData.getSpRateMin(), fishingData.getSpRateMax()) * lvlModifier);
					final int fishId = baitData.getRewards().get(Rnd.get(0, numRewards - 1));
					_player.getInventory().addItem("Fishing Reward", fishId, 1, _player, null);
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					msg.addItemName(fishId);
					_player.sendPacket(msg);
				}
				else
				{
					LOGGER.warn("Could not find fishing rewards for bait ", bait.getId());
				}
			}
			else if (reason == FishingEndReason.LOSE)
			{
				_player.sendPacket(SystemMessageId.THE_BAIT_HAS_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
			}
			
			if (consumeBait)
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerFishing(_player, reason), _player);
			}
		}
		finally
		{
			_player.broadcastPacket(new ExFishingEnd(_player, reason));
			_player.sendPacket(new ExUserInfoFishing(_player, false));
		}
	}
	
	public void stopFishing()
	{
		stopFishing(FishingEndType.PLAYER_STOP);
	}
	
	public synchronized void stopFishing(FishingEndType endType)
	{
		if (isFishing())
		{
			reelIn(FishingEndReason.STOP, false);
			_isFishing = false;
			switch (endType)
			{
				case PLAYER_STOP:
				{
					_player.sendPacket(SystemMessageId.YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING);
					break;
				}
				case PLAYER_CANCEL:
				{
					_player.sendPacket(SystemMessageId.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
					break;
				}
			}
		}
	}
	
	public ILocational getBaitLocation()
	{
		return _baitLocation;
	}
	
	private Location calculateBaitLocation()
	{
		// calculate a position in front of the player with a random distance
		int distMin = FishingData.getInstance().getBaitDistanceMin();
		int distMax = FishingData.getInstance().getBaitDistanceMax();
		int distance = Rnd.get(distMin, distMax);
		final double angle = Util.convertHeadingToDegree(_player.getHeading());
		final double radian = Math.toRadians(angle);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		int baitX = (int) (_player.getX() + (cos * distance));
		int baitY = (int) (_player.getY() + (sin * distance));
		
		// search for fishing and water zone
		FishingZone fishingZone = null;
		WaterZone waterZone = null;
		for (final ZoneType zone : ZoneManager.getInstance().getZones(baitX, baitY))
		{
			if (zone instanceof FishingZone)
			{
				fishingZone = (FishingZone) zone;
			}
			else if (zone instanceof WaterZone)
			{
				waterZone = (WaterZone) zone;
			}
			
			if ((fishingZone != null) && (waterZone != null))
			{
				break;
			}
		}
		
		int baitZ = computeBaitZ(_player, baitX, baitY, fishingZone, waterZone);
		if (baitZ == Integer.MIN_VALUE)
		{
			for (distance = distMax; distance >= distMin; --distance)
			{
				baitX = (int) (_player.getX() + (cos * distance));
				baitY = (int) (_player.getY() + (sin * distance));
				
				// search for fishing and water zone again
				fishingZone = null;
				waterZone = null;
				for (final ZoneType zone : ZoneManager.getInstance().getZones(baitX, baitY))
				{
					if (zone instanceof FishingZone)
					{
						fishingZone = (FishingZone) zone;
					}
					else if (zone instanceof WaterZone)
					{
						waterZone = (WaterZone) zone;
					}
					
					if ((fishingZone != null) && (waterZone != null))
					{
						break;
					}
				}
				
				baitZ = computeBaitZ(_player, baitX, baitY, fishingZone, waterZone);
				if (baitZ != Integer.MIN_VALUE)
				{
					break;
				}
			}
			
			if (baitZ == Integer.MIN_VALUE)
			{
				if (_player.isGM())
				{
					baitZ = _player.getZ();
				}
				else
				{
					_player.sendPacket(SystemMessageId.YOU_CAN_T_FISH_HERE);
					return null;
				}
			}
		}
		
		return new Location(baitX, baitY, baitZ);
	}
	
	/**
	 * Computes the Z of the bait.
	 * @param player the player
	 * @param baitX the bait x
	 * @param baitY the bait y
	 * @param fishingZone the fishing zone
	 * @param waterZone the water zone
	 * @return the bait z or {@link Integer#MIN_VALUE} when you cannot fish here
	 */
	private static int computeBaitZ(final PlayerInstance player, final int baitX, final int baitY, final FishingZone fishingZone, final WaterZone waterZone)
	{
		if ((fishingZone == null))
		{
			return Integer.MIN_VALUE;
		}
		
		if ((waterZone == null))
		{
			return Integer.MIN_VALUE;
		}
		
		// always use water zone, fishing zone high z is high in the air...
		int baitZ = waterZone.getWaterZ();
		
		if (!GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ(), baitX, baitY, baitZ))
		{
			return Integer.MIN_VALUE;
		}
		
		if (GeoData.getInstance().hasGeo(baitX, baitY))
		{
			if (GeoData.getInstance().getHeight(baitX, baitY, baitZ) > baitZ)
			{
				return Integer.MIN_VALUE;
			}
			
			if (GeoData.getInstance().getHeight(baitX, baitY, player.getZ()) > baitZ)
			{
				return Integer.MIN_VALUE;
			}
		}
		
		return baitZ;
	}
}
