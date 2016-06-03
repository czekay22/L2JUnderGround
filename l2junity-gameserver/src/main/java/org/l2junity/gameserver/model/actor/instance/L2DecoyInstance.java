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
package org.l2junity.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.CharInfo;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.DecayTaskManager;

public class L2DecoyInstance extends Creature
{
	private final PlayerInstance _owner;
	private int _totalLifeTime;
	private int _timeRemaining;
	private Future<?> _DecoyLifeTask;
	private Future<?> _HateSpam;
	
	public L2DecoyInstance(L2NpcTemplate template, PlayerInstance owner, int totalLifeTime)
	{
		super(template);
		setInstanceType(InstanceType.L2DecoyInstance);
		_owner = owner;
		setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		setIsInvul(false);
		_totalLifeTime = totalLifeTime;
		_timeRemaining = _totalLifeTime;
		int skilllevel = getTemplate().getDisplayId() - 13070;
		_DecoyLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DecoyLifetime(getOwner(), this), 1000, 1000);
		_HateSpam = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HateSpam(this, SkillData.getInstance().getSkill(5272, skilllevel)), 2000, 5000);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (_HateSpam != null)
		{
			_HateSpam.cancel(true);
			_HateSpam = null;
		}
		_totalLifeTime = 0;
		DecayTaskManager.getInstance().add(this);
		return true;
	}
	
	static class DecoyLifetime implements Runnable
	{
		private final PlayerInstance _activeChar;
		
		private final L2DecoyInstance _Decoy;
		
		DecoyLifetime(PlayerInstance activeChar, L2DecoyInstance Decoy)
		{
			_activeChar = activeChar;
			_Decoy = Decoy;
		}
		
		@Override
		public void run()
		{
			try
			{
				_Decoy.decTimeRemaining(1000);
				double newTimeRemaining = _Decoy.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					_Decoy.unSummon(_activeChar);
				}
			}
			catch (Exception e)
			{
				_log.error("Decoy Error: ", e);
			}
		}
	}
	
	private static class HateSpam implements Runnable
	{
		private final L2DecoyInstance _activeChar;
		private final Skill _skill;
		
		HateSpam(L2DecoyInstance activeChar, Skill Hate)
		{
			_activeChar = activeChar;
			_skill = Hate;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.setTarget(_activeChar);
				_activeChar.doCast(_skill);
			}
			catch (Throwable e)
			{
				_log.error("Decoy Error: ", e);
			}
		}
	}
	
	public void unSummon(PlayerInstance owner)
	{
		if (_DecoyLifeTask != null)
		{
			_DecoyLifeTask.cancel(true);
			_DecoyLifeTask = null;
		}
		if (_HateSpam != null)
		{
			_HateSpam.cancel(true);
			_HateSpam = null;
		}
		
		if (isSpawned() && !isDead())
		{
			ZoneManager.getInstance().getRegion(this).removeFromZones(this);
			decayMe();
		}
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		sendPacket(new CharInfo(this, false));
	}
	
	@Override
	public void updateAbnormalVisualEffects()
	{
		World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
		{
			if (isVisibleFor(player))
			{
				player.sendPacket(new CharInfo(this, isInvisible() && player.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)));
			}
		});
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public final int getId()
	{
		return getTemplate().getId();
	}
	
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	public void deleteMe(PlayerInstance owner)
	{
		decayMe();
	}
	
	public final PlayerInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public PlayerInstance getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	@Override
	public void sendInfo(PlayerInstance activeChar)
	{
		activeChar.sendPacket(new CharInfo(this, isInvisible() && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)));
	}
	
	@Override
	public void sendPacket(IClientOutgoingPacket... packets)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(packets);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(id);
		}
	}
}
