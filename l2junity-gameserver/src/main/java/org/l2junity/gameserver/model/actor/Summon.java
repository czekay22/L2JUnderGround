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
package org.l2junity.gameserver.model.actor;

import org.l2junity.Config;
import org.l2junity.commons.util.CommonUtil;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ai.CharacterAI;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.ai.SummonAI;
import org.l2junity.gameserver.data.xml.impl.ExperienceData;
import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.enums.Race;
import org.l2junity.gameserver.enums.ShotType;
import org.l2junity.gameserver.enums.Team;
import org.l2junity.gameserver.handler.IItemHandler;
import org.l2junity.gameserver.handler.ItemHandler;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.AggroInfo;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.stat.SummonStat;
import org.l2junity.gameserver.model.actor.status.SummonStatus;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.effects.EffectFlag;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerSummonSpawn;
import org.l2junity.gameserver.model.itemcontainer.PetInventory;
import org.l2junity.gameserver.model.items.EtcItem;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.items.type.ActionType;
import org.l2junity.gameserver.model.olympiad.OlympiadGameManager;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.SkillCaster;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneRegion;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExPartyPetWindowAdd;
import org.l2junity.gameserver.network.client.send.ExPartyPetWindowDelete;
import org.l2junity.gameserver.network.client.send.ExPartyPetWindowUpdate;
import org.l2junity.gameserver.network.client.send.ExPetInfo;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.PetDelete;
import org.l2junity.gameserver.network.client.send.PetInfo;
import org.l2junity.gameserver.network.client.send.PetItemList;
import org.l2junity.gameserver.network.client.send.PetStatusUpdate;
import org.l2junity.gameserver.network.client.send.RelationChanged;
import org.l2junity.gameserver.network.client.send.SummonInfo;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.TeleportToLocation;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.taskmanager.DecayTaskManager;

public abstract class Summon extends Playable
{
	private PlayerInstance _owner;
	private int _attackRange = 36; // Melee range
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	protected boolean _restoreSummon = true;
	private int _shotsMask = 0;
	private int _summonPoints = 0;
	
	// @formatter:off
	private static final int[] PASSIVE_SUMMONS =
	{
		12564, 12621, 14702, 14703, 14704, 14705, 14706, 14707, 14708, 14709, 14710, 14711,
		14712, 14713, 14714, 14715, 14716, 14717, 14718, 14719, 14720, 14721, 14722, 14723,
		14724, 14725, 14726, 14727, 14728, 14729, 14730, 14731, 14732, 14733, 14734, 14735, 14736
	};
	// @formatter:on
	
	public Summon(L2NpcTemplate template, PlayerInstance owner)
	{
		super(template);
		setInstanceType(InstanceType.L2Summon);
		setInstance(owner.getInstanceWorld()); // set instance to same as owner
		setShowSummonAnimation(true);
		_owner = owner;
		getAI();
		
		setXYZInvisible(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (Config.SUMMON_STORE_SKILL_COOLTIME && !isTeleporting())
		{
			restoreEffects();
		}
		
		setFollowStatus(true);
		updateAndBroadcastStatus(0);
		sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
		World.getInstance().forEachVisibleObjectInRange(getOwner(), PlayerInstance.class, 800, player ->
		{
			player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
		});
		Party party = getOwner().getParty();
		if (party != null)
		{
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowAdd(this));
		}
		setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
		// if someone comes into range now, the animation shouldn't show any more
		_restoreSummon = false;
		
		rechargeShots(true, true, false);
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSummonSpawn(this), this);
	}
	
	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new SummonStat(this));
	}
	
	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SummonStatus(this));
	}
	
	@Override
	protected CharacterAI initAI()
	{
		return new SummonAI(this);
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();
	
	@Override
	public final void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(1);
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(1);
	}
	
	@Override
	public void updateAbnormalVisualEffects()
	{
		World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
		{
			if (isPet())
			{
				player.sendPacket(new ExPetInfo(this, player, 1));
			}
			else
			{
				if (player == getOwner())
				{
					player.sendPacket(new PetInfo(this, 1));
				}
				else
				{
					player.sendPacket(new SummonInfo(this, player, 1));
				}
			}
		});
	}
	
	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= ExperienceData.getInstance().getMaxPetLevel())
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel());
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= (ExperienceData.getInstance().getMaxPetLevel() - 1))
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel() + 1);
	}
	
	@Override
	public final int getReputation()
	{
		return getOwner() != null ? getOwner().getReputation() : 0;
	}
	
	@Override
	public final byte getPvpFlag()
	{
		return getOwner() != null ? getOwner().getPvpFlag() : 0;
	}
	
	@Override
	public final Team getTeam()
	{
		return getOwner() != null ? getOwner().getTeam() : Team.NONE;
	}
	
	public final PlayerInstance getOwner()
	{
		return _owner;
	}
	
	/**
	 * Gets the summon ID.
	 * @return the summon ID
	 */
	@Override
	public final int getId()
	{
		return getTemplate().getId();
	}
	
	public short getSoulShotsPerHit()
	{
		if (getTemplate().getSoulShot() > 0)
		{
			return (short) getTemplate().getSoulShot();
		}
		return 1;
	}
	
	public short getSpiritShotsPerHit()
	{
		if (getTemplate().getSpiritShot() > 0)
		{
			return (short) getTemplate().getSpiritShot();
		}
		return 1;
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (isNoblesseBlessedAffected())
		{
			stopEffects(EffectFlag.NOBLESS_BLESSING);
			storeEffect(true);
		}
		else
		{
			storeEffect(false);
		}
		
		if (!super.doDie(killer))
		{
			return false;
		}
		
		final PlayerInstance owner = getOwner();
		if (owner != null)
		{
			World.getInstance().forEachVisibleObject(this, Attackable.class, TgMob ->
			{
				if (TgMob.isDead())
				{
					return;
				}
				
				final AggroInfo info = TgMob.getAggroList().get(this);
				if (info != null)
				{
					TgMob.addDamageHate(owner, info.getDamage(), info.getHate());
				}
			});
		}
		
		DecayTaskManager.getInstance().add(this);
		return true;
	}
	
	public boolean doDie(Creature killer, boolean decayed)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (!decayed)
		{
			DecayTaskManager.getInstance().add(this);
		}
		return true;
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
	public void broadcastStatusUpdate(Creature caster)
	{
		super.broadcastStatusUpdate(caster);
		updateAndBroadcastStatus(1);
	}
	
	public void deleteMe(PlayerInstance owner)
	{
		if (owner != null)
		{
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			final Party party = owner.getParty();
			if (party != null)
			{
				party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
			}
		}
		
		// pet will be deleted along with all his items
		if (getInventory() != null)
		{
			getInventory().destroyAllItems("pet deleted", getOwner(), this);
		}
		decayMe();
		if (owner != null)
		{
			if (isPet())
			{
				owner.setPet(null);
			}
			else
			{
				owner.removeServitor(getObjectId());
			}
		}
		super.deleteMe();
	}
	
	public void unSummon(PlayerInstance owner)
	{
		if (isSpawned() && !isDead())
		{
			abortAttack();
			abortCast();
			storeMe();
			storeEffect(true);
			
			// Stop AI tasks
			if (hasAI())
			{
				getAI().stopAITask(); // Calls stopFollow as well.
			}
			
			stopAllEffects();
			
			if (owner != null)
			{
				if (isPet())
				{
					owner.setPet(null);
				}
				else
				{
					owner.removeServitor(getObjectId());
				}
				
				owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
				final Party party = owner.getParty();
				if (party != null)
				{
					party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
				}
				
				if ((getInventory() != null) && (getInventory().getSize() > 0))
				{
					getOwner().setPetInvItems(true);
					sendPacket(SystemMessageId.THERE_ARE_ITEMS_IN_YOUR_PET_INVENTORY_RENDERING_YOU_UNABLE_TO_SELL_TRADE_DROP_PET_SUMMONING_ITEMS_PLEASE_EMPTY_YOUR_PET_INVENTORY);
				}
				else
				{
					getOwner().setPetInvItems(false);
				}
			}
			
			final ZoneRegion oldRegion = ZoneManager.getInstance().getRegion(this);
			decayMe();
			oldRegion.removeFromZones(this);
			
			setTarget(null);
			if (owner != null)
			{
				for (int itemId : owner.getAutoSoulShot())
				{
					String handler = ((EtcItem) ItemTable.getInstance().getTemplate(itemId)).getHandlerName();
					if ((handler != null) && handler.contains("Beast"))
					{
						owner.disableAutoShot(itemId);
					}
				}
			}
		}
	}
	
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public void setAttackRange(int range)
	{
		_attackRange = (range < 36) ? 36 : range;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return (_owner != null) && _owner.isAutoAttackable(attacker);
	}
	
	public int getControlObjectId()
	{
		return 0;
	}
	
	public Weapon getActiveWeapon()
	{
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return null;
	}
	
	public void setRestoreSummon(boolean val)
	{
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
	
	/**
	 * Return True if the L2Summon is invulnerable or if the summoner is in spawn protection.
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || getOwner().isSpawnProtected();
	}
	
	/**
	 * Return the L2Party object of its L2PcInstance owner or null.
	 */
	@Override
	public Party getParty()
	{
		if (_owner == null)
		{
			return null;
		}
		
		return _owner.getParty();
	}
	
	/**
	 * Return True if the L2Character has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		return (_owner != null) && _owner.isInParty();
	}
	
	/**
	 * Check if the active L2Skill can be casted.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Check if the target is correct</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the summon owns enough HP and MP to cast the skill</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li>
	 * <li>Check if the skill is active</li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(Skill skill, ItemInstance item, boolean forceUse, boolean dontMove)
	{
		// Null skill, dead summon or null owner are reasons to prevent casting.
		if ((skill == null) || isDead() || (getOwner() == null))
		{
			return false;
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return false;
		}
		
		// If a skill is currently being used
		if (isCastingNow(SkillCaster::isAnyNormalType))
		{
			return false;
		}
		
		// Get the target for the skill
		WorldObject target = skill.getTarget(this, forceUse, dontMove, false);
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
			return false;
		}
		
		// Check if this skill is enabled (e.g. reuse time)
		if (isSkillDisabled(skill))
		{
			sendPacket(SystemMessageId.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
			return false;
		}
		
		// Check if the summon has enough MP
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			return false;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			return false;
		}
		
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if this is bad magic skill
		if (skill.isBad())
		{
			// If L2PcInstance is in Olympiad and the match isn't already start, send a Server->Client packet ActionFailed
			if (getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized temporarily disable follow mode
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more immobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(PlayerInstance newOwner)
	{
		_owner = newOwner;
	}
	
	@Override
	public void sendDamageMessage(Creature target, Skill skill, int damage, boolean crit, boolean miss)
	{
		if (miss || (getOwner() == null))
		{
			return;
		}
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (crit)
			{
				if (isServitor())
				{
					sendPacket(SystemMessageId.SUMMONED_MONSTER_S_CRITICAL_HIT);
				}
				else
				{
					sendPacket(SystemMessageId.PET_S_CRITICAL_HIT);
				}
			}
			
			if (getOwner().isInOlympiadMode() && (target instanceof PlayerInstance) && ((PlayerInstance) target).isInOlympiadMode() && (((PlayerInstance) target).getOlympiadGameId() == getOwner().getOlympiadGameId()))
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
			}
			
			final SystemMessage sm;
			
			if ((target.isHpBlocked() && !target.isNpc()) || (target.isPlayer() && target.isAffected(EffectFlag.FACEOFF) && (target.getActingPlayer().getAttackerObjId() != getObjectId())))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_INFLICTED_S3_DAMAGE_ON_C2_S4);
				sm.addNpcName(this);
				sm.addCharName(target);
				sm.addInt(damage);
				sm.addPopup(target.getObjectId(), getObjectId(), (damage * -1));
			}
			
			sendPacket(sm);
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		
		if (!isDead() && !isHpBlocked() && (getOwner() != null) && (attacker != null) && (!getOwner().isAffected(EffectFlag.FACEOFF) || (getOwner().getAttackerObjId() == attacker.getObjectId())))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2);
			sm.addNpcName(this);
			sm.addCharName(attacker);
			sm.addInt((int) damage);
			sendPacket(sm);
		}
	}
	
	@Override
	public void doCast(Skill skill)
	{
		final PlayerInstance actingPlayer = getActingPlayer();
		if ((skill.getTarget(this, false, false, false) == null) && !actingPlayer.getAccessLevel().allowPeaceAttack())
		{
			// Send a System Message to the L2PcInstance
			actingPlayer.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		super.doCast(skill);
	}
	
	@Override
	public boolean isInCombat()
	{
		return (getOwner() != null) && getOwner().isInCombat();
	}
	
	@Override
	public PlayerInstance getActingPlayer()
	{
		return getOwner();
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		if (getOwner() == null)
		{
			return;
		}
		
		sendPacket(new PetInfo(this, val));
		sendPacket(new PetStatusUpdate(this));
		if (isSpawned())
		{
			broadcastNpcInfo(val);
		}
		Party party = getOwner().getParty();
		if (party != null)
		{
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowUpdate(this));
		}
		updateEffectIcons(true);
	}
	
	public void broadcastNpcInfo(int val)
	{
		World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
		{
			if ((player == getOwner()))
			{
				return;
			}
			
			if (isPet())
			{
				player.sendPacket(new ExPetInfo(this, player, val));
			}
			else
			{
				player.sendPacket(new SummonInfo(this, player, val));
			}
		});
	}
	
	public boolean isHungry()
	{
		return false;
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	@Override
	public void sendInfo(PlayerInstance activeChar)
	{
		// Check if the L2PcInstance is the owner of the Pet
		if (activeChar == getOwner())
		{
			activeChar.sendPacket(new PetInfo(this, 1));
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			updateEffectIcons(true);
			if (isPet())
			{
				activeChar.sendPacket(new PetItemList(getInventory().getItems()));
			}
		}
		else
		{
			if (isPet())
			{
				activeChar.sendPacket(new ExPetInfo(this, activeChar, 0));
			}
			else
			{
				activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
			}
		}
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		sendPacket(new TeleportToLocation(this, getX(), getY(), getZ(), getHeading()));
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + getId() + ") Owner: " + getOwner();
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().getRace() == Race.UNDEAD;
	}
	
	/**
	 * Change the summon's state.
	 */
	public void switchMode()
	{
		// Do nothing.
	}
	
	/**
	 * Cancel the summon's action.
	 */
	public void cancelAction()
	{
		if (!isMovementDisabled())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	/**
	 * Performs an attack to the owner's target.
	 * @param target the target to attack.
	 */
	public void doAttack(WorldObject target)
	{
		if (getOwner() != null)
		{
			if (target != null)
			{
				setTarget(target);
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}
	
	/**
	 * Verify if the summon can perform an attack.
	 * @param target the target to check if can be attacked.
	 * @param ctrlPressed {@code true} if Ctrl key is pressed
	 * @return {@code true} if the summon can attack, {@code false} otherwise
	 */
	public final boolean canAttack(WorldObject target, boolean ctrlPressed)
	{
		if (getOwner() == null)
		{
			return false;
		}
		
		if ((target == null) || (this == target) || (getOwner() == target))
		{
			return false;
		}
		
		// Sin eater, Big Boom, Wyvern can't attack with attack button.
		final int npcId = getId();
		if (CommonUtil.contains(PASSIVE_SUMMONS, npcId))
		{
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isBetrayed())
		{
			sendPacket(SystemMessageId.YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isAttackingDisabled())
		{
			if (!isAttackingNow())
			{
				return false;
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		
		if (isPet() && ((getLevel() - getOwner().getLevel()) > 20))
		{
			sendPacket(SystemMessageId.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
		{
			// If owner is in Olympiad and the match isn't already start, send a Server->Client packet ActionFailed
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((target.getActingPlayer() != null) && (getOwner().getSiegeState() > 0) && getOwner().isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeSide() == getOwner().getSiegeSide()))
		{
			sendPacket(SystemMessageId.FORCE_ATTACK_IS_IMPOSSIBLE_AGAINST_A_TEMPORARY_ALLIED_MEMBER_DURING_A_SIEGE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!getOwner().getAccessLevel().allowPeaceAttack() && getOwner().isInsidePeaceZone(this, target))
		{
			sendPacket(SystemMessageId.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
			return false;
		}
		
		if (isLockedTarget())
		{
			sendPacket(SystemMessageId.FAILED_TO_CHANGE_ENMITY);
			return false;
		}
		
		// Summons can attack NPCs even when the owner cannot.
		if (!target.isAutoAttackable(getOwner()) && !ctrlPressed && !target.isNpc())
		{
			setFollowStatus(false);
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		
		// Siege golems AI doesn't support attacking other than doors/walls at the moment.
		if (target.isDoor() && (getTemplate().getRace() != Race.SIEGE_WEAPON))
		{
			return false;
		}
		
		return true;
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
	
	@Override
	public boolean isSummon()
	{
		return true;
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic, boolean fish)
	{
		ItemInstance item;
		IItemHandler handler;
		
		if ((getOwner().getAutoSoulShot() == null) || getOwner().getAutoSoulShot().isEmpty())
		{
			return;
		}
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			item = getOwner().getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (item.getItem().getDefaultAction() == ActionType.SUMMON_SPIRITSHOT)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(getOwner(), item, false);
						}
					}
				}
				
				if (physical)
				{
					if (item.getItem().getDefaultAction() == ActionType.SUMMON_SOULSHOT)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(getOwner(), item, false);
						}
					}
				}
			}
			else
			{
				getOwner().removeAutoSoulShot(itemId);
			}
		}
	}
	
	@Override
	public int getClanId()
	{
		return (getOwner() != null) ? getOwner().getClanId() : 0;
	}
	
	@Override
	public int getAllyId()
	{
		return (getOwner() != null) ? getOwner().getAllyId() : 0;
	}
	
	public int getFormId()
	{
		int formId = 0;
		final int npcId = getId();
		if ((npcId == 16041) || (npcId == 16042))
		{
			if (getLevel() > 69)
			{
				formId = 3;
			}
			else if (getLevel() > 64)
			{
				formId = 2;
			}
			else if (getLevel() > 59)
			{
				formId = 1;
			}
		}
		else if ((npcId == 16025) || (npcId == 16037))
		{
			if (getLevel() > 69)
			{
				formId = 3;
			}
			else if (getLevel() > 64)
			{
				formId = 2;
			}
			else if (getLevel() > 59)
			{
				formId = 1;
			}
		}
		return formId;
	}
	
	public void setSummonPoints(int summonPoints)
	{
		_summonPoints = summonPoints;
	}
	
	public int getSummonPoints()
	{
		return _summonPoints;
	}
	
	public void sendInventoryUpdate(InventoryUpdate iu)
	{
		final PlayerInstance owner = getOwner();
		if (owner != null)
		{
			owner.sendInventoryUpdate(iu);
		}
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !getTemplate().canMove();
	}
	
	@Override
	public boolean isTargetable()
	{
		return super.isTargetable() && getTemplate().isTargetable();
	}
}
