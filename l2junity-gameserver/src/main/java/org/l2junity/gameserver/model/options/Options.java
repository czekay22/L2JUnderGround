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
package org.l2junity.gameserver.model.options;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SkillCoolTime;

/**
 * @author UnAfraid
 */
public class Options
{
	private final int _id;
	private final List<AbstractEffect> _effects = new ArrayList<>();
	
	private SkillHolder _activeSkill = null;
	private SkillHolder _passiveSkill = null;
	
	private final List<OptionsSkillHolder> _activationSkills = new ArrayList<>();
	
	/**
	 * @param id
	 */
	public Options(int id)
	{
		_id = id;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public void addEffect(AbstractEffect effect)
	{
		_effects.add(effect);
	}
	
	public List<AbstractEffect> getEffects()
	{
		return _effects;
	}
	
	public boolean hasEffects()
	{
		return !_effects.isEmpty();
	}
	
	public boolean hasActiveSkill()
	{
		return _activeSkill != null;
	}
	
	public SkillHolder getActiveSkill()
	{
		return _activeSkill;
	}
	
	public void setActiveSkill(SkillHolder holder)
	{
		_activeSkill = holder;
	}
	
	public boolean hasPassiveSkill()
	{
		return _passiveSkill != null;
	}
	
	public SkillHolder getPassiveSkill()
	{
		return _passiveSkill;
	}
	
	public void setPassiveSkill(SkillHolder holder)
	{
		_passiveSkill = holder;
	}
	
	public boolean hasActivationSkills()
	{
		return !_activationSkills.isEmpty();
	}
	
	public boolean hasActivationSkills(OptionsSkillType type)
	{
		for (OptionsSkillHolder holder : _activationSkills)
		{
			if (holder.getSkillType() == type)
			{
				return true;
			}
		}
		return false;
	}
	
	public List<OptionsSkillHolder> getActivationsSkills()
	{
		return _activationSkills;
	}
	
	public List<OptionsSkillHolder> getActivationsSkills(OptionsSkillType type)
	{
		List<OptionsSkillHolder> temp = new ArrayList<>();
		for (OptionsSkillHolder holder : _activationSkills)
		{
			if (holder.getSkillType() == type)
			{
				temp.add(holder);
			}
		}
		return temp;
	}
	
	public void addActivationSkill(OptionsSkillHolder holder)
	{
		_activationSkills.add(holder);
	}
	
	public void apply(PlayerInstance player)
	{
		player.sendDebugMessage("Activating option id: " + _id);
		if (hasEffects())
		{
			final BuffInfo info = new BuffInfo(player, player, null, true, null, this);
			for (AbstractEffect effect : _effects)
			{
				if (effect.isInstant())
				{
					if (effect.calcSuccess(info.getEffector(), info.getEffected(), info.getSkill()))
					{
						effect.instant(info.getEffector(), info.getEffected(), info.getSkill(), info.getItem());
					}
					player.sendDebugMessage("Appling instant effect: " + effect.getClass().getSimpleName());
				}
				else
				{
					effect.continuousInstant(info.getEffector(), info.getEffected(), info.getSkill(), info.getItem());
					effect.pump(player, info.getSkill());
					
					if (effect.canStart(info))
					{
						info.addEffect(effect);
					}
					
					player.sendDebugMessage("Appling continious effect: " + effect.getClass().getSimpleName());
				}
			}
			if (!info.getEffects().isEmpty())
			{
				player.getEffectList().add(info);
			}
		}
		if (hasActiveSkill())
		{
			addSkill(player, getActiveSkill().getSkill());
			player.sendDebugMessage("Adding active skill: " + getActiveSkill());
		}
		if (hasPassiveSkill())
		{
			addSkill(player, getPassiveSkill().getSkill());
			player.sendDebugMessage("Adding passive skill: " + getPassiveSkill());
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.addTriggerSkill(holder);
				player.sendDebugMessage("Adding trigger skill: " + holder);
			}
		}
		
		player.getStat().recalculateStats(true);
		player.sendSkillList();
	}
	
	public void remove(PlayerInstance player)
	{
		player.sendDebugMessage("Deactivating option id: " + _id);
		if (hasEffects())
		{
			for (BuffInfo info : player.getEffectList().getOptions())
			{
				if (info.getOption() == this)
				{
					player.sendDebugMessage("Removing effects: " + info.getEffects());
					player.getEffectList().remove(false, info, this);
				}
			}
		}
		if (hasActiveSkill())
		{
			player.removeSkill(getActiveSkill().getSkill(), false, false);
			player.sendDebugMessage("Removing active skill: " + getActiveSkill());
		}
		if (hasPassiveSkill())
		{
			player.removeSkill(getPassiveSkill().getSkill(), false, true);
			player.sendDebugMessage("Removing passive skill: " + getPassiveSkill());
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.removeTriggerSkill(holder);
				player.sendDebugMessage("Removing trigger skill: " + holder);
			}
		}
		
		player.getStat().recalculateStats(true);
		player.sendSkillList();
	}
	
	private void addSkill(PlayerInstance player, Skill skill)
	{
		boolean updateTimeStamp = false;
		
		player.addSkill(skill, false);
		
		if (skill.isActive())
		{
			final long remainingTime = player.getSkillRemainingReuseTime(skill.getReuseHashCode());
			if (remainingTime > 0)
			{
				player.addTimeStamp(skill, remainingTime);
				player.disableSkill(skill, remainingTime);
			}
			updateTimeStamp = true;
		}
		if (updateTimeStamp)
		{
			player.sendPacket(new SkillCoolTime(player));
		}
	}
}
