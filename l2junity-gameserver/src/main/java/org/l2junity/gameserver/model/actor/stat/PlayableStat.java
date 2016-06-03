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
package org.l2junity.gameserver.model.actor.stat;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.ExperienceData;
import org.l2junity.gameserver.data.xml.impl.PetDataTable;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayableExpChanged;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.network.client.send.ExNewSkillToLearnByLevelUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayableStat extends CharStat
{
	protected static final Logger _log = LoggerFactory.getLogger(PlayableStat.class);
	
	public PlayableStat(Playable activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(long value)
	{
		final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnPlayableExpChanged(getActiveChar(), getExp(), getExp() + value), getActiveChar(), TerminateReturn.class);
		if ((term != null) && term.terminate())
		{
			return false;
		}
		
		if (((getExp() + value) < 0) || ((value > 0) && (getExp() == (getExpForLevel(getMaxLevel()) - 1))))
		{
			return true;
		}
		
		if ((getExp() + value) >= getExpForLevel(getMaxLevel()))
		{
			value = getExpForLevel(getMaxLevel()) - 1 - getExp();
		}
		
		final int oldLevel = getLevel();
		setExp(getExp() + value);
		
		byte minimumLevel = 1;
		if (getActiveChar().isPet())
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getId());
		}
		
		byte level = minimumLevel; // minimum level
		
		for (byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		
		if ((level != getLevel()) && (level >= minimumLevel))
		{
			addLevel((byte) (level - getLevel()));
		}
		
		if ((getLevel() > oldLevel) && getActiveChar().isPlayer())
		{
			final PlayerInstance activeChar = getActiveChar().getActingPlayer();
			if (SkillTreesData.getInstance().hasAvailableSkills(activeChar, activeChar.getClassId()))
			{
				getActiveChar().sendPacket(ExNewSkillToLearnByLevelUp.STATIC_PACKET);
			}
		}
		
		return true;
	}
	
	public boolean removeExp(long value)
	{
		if ((getExp() - value) < 0)
		{
			value = getExp() - 1;
		}
		
		setExp(getExp() - value);
		
		byte minimumLevel = 1;
		if (getActiveChar().isPet())
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getId());
		}
		byte level = minimumLevel;
		
		for (byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if ((level != getLevel()) && (level >= minimumLevel))
		{
			addLevel((byte) (level - getLevel()));
		}
		return true;
	}
	
	public boolean removeExpAndSp(long removeExp, long removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if (removeExp > 0)
		{
			expRemoved = removeExp(removeExp);
		}
		if (removeSp > 0)
		{
			spRemoved = removeSp(removeSp);
		}
		
		return expRemoved || spRemoved;
	}
	
	public boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (getMaxLevel() - 1))
		{
			if (getLevel() < (getMaxLevel() - 1))
			{
				value = (byte) (getMaxLevel() - 1 - getLevel());
			}
			else
			{
				return false;
			}
		}
		
		boolean levelIncreased = ((getLevel() + value) > getLevel());
		value += getLevel();
		setLevel(value);
		
		// Sync up exp with current level
		if ((getExp() >= getExpForLevel(getLevel() + 1)) || (getExpForLevel(getLevel()) > getExp()))
		{
			setExp(getExpForLevel(getLevel()));
		}
		
		if (!levelIncreased && (getActiveChar() instanceof PlayerInstance) && !getActiveChar().isGM() && Config.DECREASE_SKILL_LEVEL)
		{
			((PlayerInstance) getActiveChar()).checkPlayerSkills();
		}
		
		if (!levelIncreased)
		{
			return false;
		}
		
		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());
		
		return true;
	}
	
	public boolean addSp(long value)
	{
		if (value < 0)
		{
			_log.warn("wrong usage");
			return false;
		}
		long currentSp = getSp();
		if (currentSp >= Config.MAX_SP)
		{
			return false;
		}
		
		if (currentSp > (Config.MAX_SP - value))
		{
			value = Config.MAX_SP - currentSp;
		}
		
		setSp(currentSp + value);
		return true;
	}
	
	public boolean removeSp(long value)
	{
		long currentSp = getSp();
		if (currentSp < value)
		{
			value = currentSp;
		}
		setSp(getSp() - value);
		return true;
	}
	
	public long getExpForLevel(int level)
	{
		return level;
	}
	
	@Override
	public Playable getActiveChar()
	{
		return (Playable) super.getActiveChar();
	}
	
	public int getMaxLevel()
	{
		return ExperienceData.getInstance().getMaxLevel();
	}
	
	@Override
	public int getPhysicalAttackRadius()
	{
		final Weapon weapon = getActiveChar().getActiveWeaponItem();
		return weapon != null ? weapon.getBaseAttackRadius() : super.getPhysicalAttackRadius();
	}
	
	@Override
	public int getPhysicalAttackAngle()
	{
		final Weapon weapon = getActiveChar().getActiveWeaponItem();
		return weapon != null ? weapon.getBaseAttackAngle() : super.getPhysicalAttackAngle();
	}
}
