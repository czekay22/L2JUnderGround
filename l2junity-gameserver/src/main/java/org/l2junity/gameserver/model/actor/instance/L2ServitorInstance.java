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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.l2junity.Config;
import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.data.sql.impl.CharSummonTable;
import org.l2junity.gameserver.data.sql.impl.SummonEffectsTable;
import org.l2junity.gameserver.data.sql.impl.SummonEffectsTable.SummonEffect;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.enums.AttributeType;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.EffectScope;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SetSummonRemainTime;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class L2ServitorInstance extends Summon implements Runnable
{
	protected static final Logger log = LoggerFactory.getLogger(L2ServitorInstance.class);
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,remaining_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
	
	private float _expMultiplier = 0;
	private ItemHolder _itemConsume;
	private int _lifeTime;
	private int _lifeTimeRemaining;
	private int _consumeItemInterval;
	private int _consumeItemIntervalRemaining;
	protected Future<?> _summonLifeTask;
	
	private int _referenceSkill;
	
	public L2ServitorInstance(L2NpcTemplate template, PlayerInstance owner)
	{
		super(template, owner);
		setInstanceType(InstanceType.L2ServitorInstance);
		setShowSummonAnimation(true);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if ((_lifeTime > 0) && (_summonLifeTask == null))
		{
			_summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 0, 5000);
		}
	}
	
	@Override
	public final int getLevel()
	{
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
	@Override
	public int getSummonType()
	{
		return 1;
	}
	
	// ************************************/
	
	public void setExpMultiplier(float expMultiplier)
	{
		_expMultiplier = expMultiplier;
	}
	
	public float getExpMultiplier()
	{
		return _expMultiplier;
	}
	
	// ************************************/
	
	public void setItemConsume(ItemHolder item)
	{
		_itemConsume = item;
	}
	
	public ItemHolder getItemConsume()
	{
		return _itemConsume;
	}
	
	// ************************************/
	
	public void setItemConsumeInterval(int interval)
	{
		_consumeItemInterval = interval;
		_consumeItemIntervalRemaining = interval;
	}
	
	public int getItemConsumeInterval()
	{
		return _consumeItemInterval;
	}
	
	// ************************************/
	
	public void setLifeTime(int lifeTime)
	{
		_lifeTime = lifeTime;
		_lifeTimeRemaining = lifeTime;
	}
	
	public int getLifeTime()
	{
		return _lifeTime;
	}
	
	// ************************************/
	
	public void setLifeTimeRemaining(int time)
	{
		_lifeTimeRemaining = time;
	}
	
	public int getLifeTimeRemaining()
	{
		return _lifeTimeRemaining;
	}
	
	// ************************************/
	
	public void setReferenceSkill(int skillId)
	{
		_referenceSkill = skillId;
	}
	
	public int getReferenceSkill()
	{
		return _referenceSkill;
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
		}
		
		CharSummonTable.getInstance().removeServitor(getOwner(), getObjectId());
		return true;
		
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level.<br>
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels.<br>
	 * After that, it is 1 skill level per 5 servitor levels.<br>
	 * If the resulting skill level doesn't exist use the max that does exist!
	 */
	@Override
	public void doCast(Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}
		
		// Adjust the level for servitors less than level 1.
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}
		
		final Skill skillToCast = SkillData.getInstance().getSkill(skill.getId(), skillLevel);
		
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}
	
	@Override
	public void setRestoreSummon(boolean val)
	{
		_restoreSummon = val;
	}
	
	@Override
	public final void stopSkillEffects(boolean removed, int skillId)
	{
		super.stopSkillEffects(removed, skillId);
		final Map<Integer, List<SummonEffect>> servitorEffects = SummonEffectsTable.getInstance().getServitorEffects(getOwner());
		if (servitorEffects != null)
		{
			final List<SummonEffect> effects = servitorEffects.get(getReferenceSkill());
			if ((effects != null) && !effects.isEmpty())
			{
				for (SummonEffect effect : effects)
				{
					final Skill skill = effect.getSkill();
					if ((skill != null) && (skill.getId() == skillId))
					{
						effects.remove(effect);
					}
				}
			}
		}
	}
	
	@Override
	public void storeMe()
	{
		if ((_referenceSkill == 0) || isDead())
		{
			return;
		}
		
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			CharSummonTable.getInstance().saveSummon(this);
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (!Config.SUMMON_STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		if ((getOwner() == null) || getOwner().isInOlympiadMode())
		{
			return;
		}
		
		// Clear list for overwrite
		if (SummonEffectsTable.getInstance().getServitorEffectsOwner().getOrDefault(getOwner().getObjectId(), Collections.emptyMap()).containsKey(getOwner().getClassIndex()))
		{
			SummonEffectsTable.getInstance().getServitorEffects(getOwner()).getOrDefault(getReferenceSkill(), Collections.emptyList()).clear();
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
		{
			// Delete all current stored effects for summon to avoid dupe
			statement.setInt(1, getOwner().getObjectId());
			statement.setInt(2, getOwner().getClassIndex());
			statement.setInt(3, getReferenceSkill());
			statement.execute();
			
			int buff_index = 0;
			
			final List<Long> storedSkills = new CopyOnWriteArrayList<>();
			
			// Store all effect data along with calculated remaining
			if (storeEffects)
			{
				try (PreparedStatement ps2 = con.prepareStatement(ADD_SKILL_SAVE))
				{
					for (BuffInfo info : getEffectList().getEffects())
					{
						if (info == null)
						{
							continue;
						}
						
						final Skill skill = info.getSkill();
						
						// Do not store those effects.
						if (skill.isDeleteAbnormalOnLeave())
						{
							continue;
						}
						
						// Do not save heals.
						if (skill.getAbnormalType() == AbnormalType.LIFE_FORCE_OTHERS)
						{
							continue;
						}
						
						// Toggles are skipped, unless they are necessary to be always on.
						if (skill.isToggle() && !skill.isNecessaryToggle())
						{
							continue;
						}
						
						// Dances and songs are not kept in retail.
						if (skill.isDance() && !Config.ALT_STORE_DANCES)
						{
							continue;
						}
						
						if (storedSkills.contains(skill.getReuseHashCode()))
						{
							continue;
						}
						
						storedSkills.add(skill.getReuseHashCode());
						
						ps2.setInt(1, getOwner().getObjectId());
						ps2.setInt(2, getOwner().getClassIndex());
						ps2.setInt(3, getReferenceSkill());
						ps2.setInt(4, skill.getId());
						ps2.setInt(5, skill.getLevel());
						ps2.setInt(6, info.getTime());
						ps2.setInt(7, ++buff_index);
						ps2.execute();
						
						// XXX: Rework me!
						if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()))
						{
							SummonEffectsTable.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new HashMap<>());
						}
						if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()))
						{
							SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new HashMap<>());
						}
						if (!SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
						{
							SummonEffectsTable.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new CopyOnWriteArrayList<>());
						}
						
						SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(new SummonEffect(skill, info.getTime()));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not store summon effect data: ", e);
		}
	}
	
	@Override
	public void restoreEffects()
	{
		if (getOwner().isInOlympiadMode())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()) || !SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()) || !SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
			{
				try (PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE))
				{
					statement.setInt(1, getOwner().getObjectId());
					statement.setInt(2, getOwner().getClassIndex());
					statement.setInt(3, getReferenceSkill());
					try (ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							int effectCurTime = rset.getInt("remaining_time");
							
							final Skill skill = SkillData.getInstance().getSkill(rset.getInt("skill_id"), rset.getInt("skill_level"));
							if (skill == null)
							{
								continue;
							}
							
							// XXX: Rework me!
							if (skill.hasEffects(EffectScope.GENERAL))
							{
								if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()))
								{
									SummonEffectsTable.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new HashMap<>());
								}
								if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()))
								{
									SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new HashMap<>());
								}
								if (!SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
								{
									SummonEffectsTable.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new CopyOnWriteArrayList<>());
								}
								
								SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(new SummonEffect(skill, effectCurTime));
							}
						}
					}
				}
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
			{
				statement.setInt(1, getOwner().getObjectId());
				statement.setInt(2, getOwner().getClassIndex());
				statement.setInt(3, getReferenceSkill());
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
		finally
		{
			if (!SummonEffectsTable.getInstance().getServitorEffectsOwner().containsKey(getOwner().getObjectId()) || !SummonEffectsTable.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).containsKey(getOwner().getClassIndex()) || !SummonEffectsTable.getInstance().getServitorEffects(getOwner()).containsKey(getReferenceSkill()))
			{
				return;
			}
			
			for (SummonEffect se : SummonEffectsTable.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()))
			{
				if (se != null)
				{
					se.getSkill().applyEffects(this, this, false, se.getEffectCurTime());
				}
			}
		}
	}
	
	@Override
	public void unSummon(PlayerInstance owner)
	{
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
		}
		
		super.unSummon(owner);
		
		if (!_restoreSummon)
		{
			CharSummonTable.getInstance().removeServitor(owner, getObjectId());
		}
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, WorldObject reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, WorldObject reference, boolean sendMessage)
	{
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	@Override
	public AttributeType getAttackElement()
	{
		if (getOwner() != null)
		{
			return getOwner().getAttackElement();
		}
		return super.getAttackElement();
	}
	
	@Override
	public int getAttackElementValue(AttributeType attackAttribute)
	{
		if (getOwner() != null)
		{
			return (getOwner().getAttackElementValue(attackAttribute));
		}
		return super.getAttackElementValue(attackAttribute);
	}
	
	@Override
	public int getDefenseElementValue(AttributeType defenseAttribute)
	{
		if (getOwner() != null)
		{
			return (getOwner().getDefenseElementValue(defenseAttribute));
		}
		return super.getDefenseElementValue(defenseAttribute);
	}
	
	@Override
	public boolean isServitor()
	{
		return true;
	}
	
	@Override
	public void run()
	{
		int usedtime = 5000;
		_lifeTimeRemaining -= usedtime;
		
		if (isDead() || !isSpawned())
		{
			if (_summonLifeTask != null)
			{
				_summonLifeTask.cancel(false);
			}
			return;
		}
		
		// check if the summon's lifetime has ran out
		if (_lifeTimeRemaining < 0)
		{
			sendPacket(SystemMessageId.YOUR_SERVITOR_PASSED_AWAY);
			unSummon(getOwner());
			return;
		}
		
		if (_consumeItemInterval > 0)
		{
			_consumeItemIntervalRemaining -= usedtime;
			
			// check if it is time to consume another item
			if ((_consumeItemIntervalRemaining <= 0) && (getItemConsume().getCount() > 0) && (getItemConsume().getId() > 0) && !isDead())
			{
				if (destroyItemByItemId("Consume", getItemConsume().getId(), getItemConsume().getCount(), this, false))
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.A_SUMMONED_MONSTER_USES_S1);
					msg.addItemName(getItemConsume().getId());
					sendPacket(msg);
					
					// Reset
					_consumeItemIntervalRemaining = _consumeItemInterval;
				}
				else
				{
					sendPacket(SystemMessageId.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITOR_S_STAY_THE_SERVITOR_HAS_DISAPPEARED);
					unSummon(getOwner());
				}
			}
		}
		
		sendPacket(new SetSummonRemainTime(getLifeTime(), _lifeTimeRemaining));
		updateEffectIcons();
	}
	
	@Override
	public void doPickupItem(WorldObject object)
	{
	}
}
