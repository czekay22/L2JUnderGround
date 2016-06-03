/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.gameserver.model.skills;

import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.data.xml.impl.ActionData;
import org.l2junity.gameserver.datatables.ItemTable;
import org.l2junity.gameserver.enums.ItemSkillType;
import org.l2junity.gameserver.enums.StatusUpdateType;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.Summon;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.L2EffectType;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureSkillFinishCast;
import org.l2junity.gameserver.model.events.impl.character.OnCreatureSkillUse;
import org.l2junity.gameserver.model.events.impl.character.npc.OnNpcSkillSee;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.holders.ItemSkillHolder;
import org.l2junity.gameserver.model.holders.SkillUseHolder;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.Weapon;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.options.OptionsSkillHolder;
import org.l2junity.gameserver.model.options.OptionsSkillType;
import org.l2junity.gameserver.model.skills.targets.TargetType;
import org.l2junity.gameserver.model.stats.Formulas;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ExRotation;
import org.l2junity.gameserver.network.client.send.FlyToLocation;
import org.l2junity.gameserver.network.client.send.MagicSkillCanceld;
import org.l2junity.gameserver.network.client.send.MagicSkillLaunched;
import org.l2junity.gameserver.network.client.send.MagicSkillUse;
import org.l2junity.gameserver.network.client.send.MoveToPawn;
import org.l2junity.gameserver.network.client.send.SetupGauge;
import org.l2junity.gameserver.network.client.send.StatusUpdate;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nik
 */
public class SkillCaster implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SkillCaster.class);
	
	private final WeakReference<Creature> _caster;
	private final WeakReference<WorldObject> _target;
	private final Skill _skill;
	private final ItemInstance _item;
	private final SkillCastingType _castingType;
	private final int _castTime;
	private int _coolTime;
	private Collection<WorldObject> _targets;
	private ScheduledFuture<?> _task;
	private int _phase;
	
	private SkillCaster(Creature caster, WorldObject target, Skill skill, ItemInstance item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed, int castTime)
	{
		Objects.requireNonNull(caster);
		Objects.requireNonNull(skill);
		Objects.requireNonNull(castingType);
		
		_caster = new WeakReference<>(caster);
		_target = new WeakReference<>(target);
		_skill = skill;
		_item = item;
		_castingType = castingType;
		_castTime = castTime;
	}
	
	/**
	 * Checks if the caster can cast the specified skill on the given target with the selected parameters.
	 * @param caster the creature trying to cast
	 * @param target the selected target for cast
	 * @param skill the skill being cast
	 * @param item the reference item which requests the skill cast
	 * @param castingType the type of casting
	 * @param ctrlPressed force casting
	 * @param shiftPressed dont move while casting
	 * @return {@code SkillCaster} object containing casting data if casting has started or {@code null} if casting was not started.
	 */
	public static SkillCaster castSkill(Creature caster, WorldObject target, Skill skill, ItemInstance item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed)
	{
		return castSkill(caster, target, skill, item, castingType, ctrlPressed, shiftPressed, -1);
	}
	
	/**
	 * Checks if the caster can cast the specified skill on the given target with the selected parameters.
	 * @param caster the creature trying to cast
	 * @param target the selected target for cast
	 * @param skill the skill being cast
	 * @param item the reference item which requests the skill cast
	 * @param castingType the type of casting
	 * @param ctrlPressed force casting
	 * @param shiftPressed dont move while casting
	 * @param castTime custom cast time in milliseconds or -1 for default.
	 * @return {@code SkillCaster} object containing casting data if casting has started or {@code null} if casting was not started.
	 */
	public static SkillCaster castSkill(Creature caster, WorldObject target, Skill skill, ItemInstance item, SkillCastingType castingType, boolean ctrlPressed, boolean shiftPressed, int castTime)
	{
		if ((caster == null) || (skill == null) || (castingType == null))
		{
			return null;
		}
		
		if (!checkUseConditions(caster, skill, castingType))
		{
			return null;
		}
		
		// Check true aiming target of the skill.
		target = skill.getTarget(caster, target, ctrlPressed, shiftPressed, false);
		if (target == null)
		{
			return null;
		}
		
		castTime = castTime > -1 ? castTime : Formulas.calcHitTime(caster, skill);
		
		// Schedule a thread that will execute 500ms before casting time is over (for animation issues and retail handling).
		SkillCaster skillCaster = new SkillCaster(caster, target, skill, item, castingType, ctrlPressed, shiftPressed, castTime);
		skillCaster.run();
		return skillCaster;
	}
	
	@Override
	public void run()
	{
		final boolean instantCast = (_castingType == SkillCastingType.SIMULTANEOUS) || _skill.isAbnormalInstant() || _skill.isWithoutAction() || _skill.isToggle();
		
		// Skills with instant cast are never launched.
		if (instantCast)
		{
			triggerCast(_caster.get(), _target.get(), _skill, _item, false);
			return;
		}
		
		long nextTaskDelay = 0;
		boolean hasNextPhase = false;
		switch (_phase++)
		{
			case 0: // Start skill casting.
			{
				hasNextPhase = startCasting();
				nextTaskDelay = _castTime;
				break;
			}
			case 1: // Launch the skill.
			{
				hasNextPhase = launchSkill();
				nextTaskDelay = Formulas.SKILL_LAUNCH_TIME;
				break;
			}
			case 2: // Finish launching and apply effects.
			{
				hasNextPhase = finishSkill();
				nextTaskDelay = _coolTime;
				break;
			}
		}
		
		// Reschedule next task if we have such.
		if (hasNextPhase)
		{
			_task = ThreadPoolManager.getInstance().scheduleEffect(this, nextTaskDelay);
		}
		else
		{
			// Stop casting if there is no next phase.
			stopCasting(false);
		}
		
	}
	
	public boolean startCasting()
	{
		final Creature caster = _caster.get();
		final WorldObject target = _target.get();
		
		if ((caster == null) || (target == null))
		{
			return false;
		}
		
		_coolTime = Formulas.calcAtkSpd(caster, _skill, _skill.getCoolTime()); // TODO Get proper fomula of this.
		final int displayedCastTime = _castTime + Formulas.SKILL_LAUNCH_TIME; // For client purposes, it must be displayed to player the skill casting time + launch time.
		final boolean instantCast = (_castingType == SkillCastingType.SIMULTANEOUS) || _skill.isAbnormalInstant() || _skill.isWithoutAction();
		
		// Add this SkillCaster to the creature so it can be marked as casting.
		if (!instantCast)
		{
			caster.addSkillCaster(_castingType, this);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		int reuseDelay = caster.getStat().getReuseTime(_skill);
		if (reuseDelay > 10)
		{
			if (Formulas.calcSkillMastery(caster, _skill))
			{
				reuseDelay = 100;
				caster.sendPacket(SystemMessageId.A_SKILL_IS_READY_TO_BE_USED_AGAIN);
			}
			
			if (reuseDelay > 30000)
			{
				caster.addTimeStamp(_skill, reuseDelay);
			}
			else
			{
				caster.disableSkill(_skill, reuseDelay);
			}
		}
		
		// Stop movement when casting. Except instant cast.
		if (!instantCast)
		{
			caster.getAI().clientStopMoving(null);
		}
		
		// Reduce talisman mana on skill use
		if ((_skill.getReferenceItemId() > 0) && (ItemTable.getInstance().getTemplate(_skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO))
		{
			ItemInstance talisman = caster.getInventory().getItems(i -> i.getId() == _skill.getReferenceItemId(), ItemInstance::isEquipped).stream().findAny().orElse(null);
			if (talisman != null)
			{
				talisman.decreaseMana(false, talisman.useSkillDisTime());
			}
		}
		
		if (target != caster)
		{
			// Face the target
			caster.setHeading(Util.calculateHeadingFrom(caster, target));
			caster.broadcastPacket(new ExRotation(caster.getObjectId(), caster.getHeading())); // TODO: Not sent in retail. Probably moveToPawn is enough
			
			// Send MoveToPawn packet to trigger Blue Bubbles on target become Red, but don't do it while (double) casting, because that will screw up animation... some fucked up stuff, right?
			if (caster.isPlayer() && !caster.isCastingNow() && target.isCreature())
			{
				caster.sendPacket(new MoveToPawn(caster, target, (int) caster.calculateDistance(target, false, false)));
				caster.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		
		// Stop effects since we started casting. It should be sent before casting bar and mana consume.
		caster.stopEffectsOnAction();
		
		// Consume skill initial MP needed for cast. Retail sends it regardless if > 0 or not.
		int initmpcons = caster.getStat().getMpInitialConsume(_skill);
		if (initmpcons > 0)
		{
			if (initmpcons > caster.getCurrentMp())
			{
				caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				return false;
			}
			
			caster.getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(caster);
			su.addUpdate(StatusUpdateType.CUR_MP, (int) caster.getCurrentMp());
			caster.sendPacket(su);
		}
		
		// Send a packet starting the casting.
		final int actionId = caster.isSummon() ? ActionData.getInstance().getSkillActionId(_skill.getId()) : -1;
		if (!_skill.isNotBroadcastable())
		{
			caster.broadcastPacket(new MagicSkillUse(caster, target, _skill.getDisplayId(), _skill.getDisplayLevel(), displayedCastTime, reuseDelay, _skill.getReuseDelayGroup(), actionId, _castingType));
		}
		
		if (caster.isPlayer() && !instantCast)
		{
			// Send a system message to the player.
			caster.sendPacket(_skill.getId() != 2046 ? SystemMessage.getSystemMessage(SystemMessageId.YOU_USE_S1).addSkillName(_skill) : SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_YOUR_PET));
			
			// Show the gauge bar for casting.
			caster.sendPacket(new SetupGauge(caster.getObjectId(), SetupGauge.BLUE, displayedCastTime));
		}
		
		// Consume the required items. Should happen after use message is displayed and SetupGauge
		if ((_skill.getItemConsumeId() > 0) && (_skill.getItemConsumeCount() > 0) && !caster.destroyItemByItemId(_skill.toString(), _skill.getItemConsumeId(), _skill.getItemConsumeCount(), null, true))
		{
			return false;
		}
		
		// Trigger any skill cast start effects.
		if (target.isCreature())
		{
			_skill.applyEffectScope(EffectScope.START, new BuffInfo(caster, (Creature) target, _skill, false, _item, null), true, false);
		}
		
		// Start channeling if skill is channeling.
		if (_skill.isChanneling() && (_skill.getChannelingSkillId() > 0))
		{
			caster.getSkillChannelizer().startChanneling(_skill);
		}
		
		return true;
	}
	
	public boolean launchSkill()
	{
		final Creature caster = _caster.get();
		final WorldObject target = _target.get();
		
		if ((caster == null) || (target == null))
		{
			return false;
		}
		
		// Gather list of affected targets by this skill.
		_targets = _skill.getTargetsAffected(caster, target);
		
		// Finish flying by setting the target location after picking targets. Packet is sent before MagicSkillLaunched.
		if (_skill.getFlyType() != null)
		{
			caster.broadcastPacket(new FlyToLocation(caster, target, _skill.getFlyType()));
			caster.setXYZ(target.getX(), target.getY(), target.getZ());
		}
		
		// Display animation of launching skill upon targets.
		if (!_skill.isNotBroadcastable())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, _skill.getDisplayId(), _skill.getDisplayLevel(), _castingType, _targets));
		}
		return true;
	}
	
	public boolean finishSkill()
	{
		final Creature caster = _caster.get();
		final WorldObject target = _target.get();
		
		if ((caster == null) || (target == null))
		{
			return false;
		}
		
		if (_targets == null)
		{
			_targets = Collections.singletonList(target);
		}
		
		final StatusUpdate su = new StatusUpdate(caster);
		
		// Consume the required MP or stop casting if not enough.
		double mpConsume = _skill.getMpConsume() > 0 ? caster.getStat().getMpConsume(_skill) : 0;
		if (mpConsume > 0)
		{
			if (mpConsume > caster.getCurrentMp())
			{
				caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				return false;
			}
			
			caster.getStatus().reduceMp(mpConsume);
			su.addUpdate(StatusUpdateType.CUR_MP, (int) caster.getCurrentMp());
		}
		
		// Consume the required HP or stop casting if not enough.
		double consumeHp = _skill.getHpConsume();
		if (consumeHp > 0)
		{
			if (consumeHp >= caster.getCurrentHp())
			{
				caster.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
				return false;
			}
			
			caster.getStatus().reduceHp(consumeHp, caster, true);
			su.addUpdate(StatusUpdateType.CUR_HP, (int) caster.getCurrentHp());
		}
		
		// Send HP/MP consumption packet if any attribute is set.
		if (su.hasUpdates())
		{
			caster.sendPacket(su);
		}
		
		// Consume Souls if necessary
		if (caster.isPlayer() && (_skill.getMaxSoulConsumeCount() > 0) && !caster.getActingPlayer().decreaseSouls(_skill.getMaxSoulConsumeCount(), _skill))
		{
			return false;
		}
		
		// Noptify skill is casted.
		EventDispatcher.getInstance().notifyEvent(new OnCreatureSkillFinishCast(caster, target, _skill, _skill.isWithoutAction()), caster);
		
		// Call the skill's effects and AI interraction and stuff.
		SkillCaster.callSkill(caster, target, _targets, _skill, _item);
		
		// Start attack stance.
		if (!_skill.isWithoutAction())
		{
			if (_skill.isBad() && (_skill.getTargetType() != TargetType.DOOR_TREASURE))
			{
				caster.getAI().clientStartAutoAttack();
			}
		}
		
		// Notify DP Scripts
		caster.notifyQuestEventSkillFinished(_skill, target);
		
		// On each repeat recharge shots before cast.
		caster.rechargeShots(_skill.useSoulShot(), _skill.useSpiritShot(), false);
		return true;
	}
	
	public static void callSkill(Creature caster, WorldObject target, Collection<WorldObject> targets, Skill skill, ItemInstance item)
	{
		// Launch the magic skill in order to calculate its effects
		try
		{
			// Check if the toggle skill effects are already in progress on the L2Character
			if (skill.isToggle() && caster.isAffectedBySkill(skill.getId()))
			{
				return;
			}
			
			// Initial checks
			for (WorldObject obj : targets)
			{
				if ((obj == null) || !obj.isCreature())
				{
					continue;
				}
				
				final Creature creature = (Creature) obj;
				
				// Check raid monster/minion attack and check buffing characters who attack raid monsters. Raid is still affected by skills.
				if (!Config.RAID_DISABLE_CURSE && creature.isRaid() && creature.giveRaidCurse() && (caster.getLevel() >= (creature.getLevel() + 9)))
				{
					if (skill.isBad() || ((creature.getTarget() == caster) && ((Attackable) creature).getAggroList().containsKey(caster)))
					{
						// Skills such as Summon Battle Scar too can trigger magic silence.
						final CommonSkill curse = skill.isBad() ? CommonSkill.RAID_CURSE2 : CommonSkill.RAID_CURSE;
						Skill curseSkill = curse.getSkill();
						if (curseSkill != null)
						{
							curseSkill.applyEffects(creature, caster);
						}
					}
				}
				
				// Static skills not trigger any chance skills
				if (!skill.isStatic())
				{
					Weapon activeWeapon = caster.getActiveWeaponItem();
					// Launch weapon Special ability skill effect if available
					if ((activeWeapon != null) && !creature.isDead())
					{
						activeWeapon.applyConditionalSkills(caster, creature, skill, ItemSkillType.ON_MAGIC_SKILL);
					}
					
					if (caster.hasTriggerSkills())
					{
						for (OptionsSkillHolder holder : caster.getTriggerSkills().values())
						{
							if ((skill.isMagic() && (holder.getSkillType() == OptionsSkillType.MAGIC)) || (skill.isPhysical() && (holder.getSkillType() == OptionsSkillType.ATTACK)))
							{
								if (Rnd.get(100) < holder.getChance())
								{
									triggerCast(caster, creature, holder.getSkill(), null, false);
								}
							}
						}
					}
				}
			}
			
			// Launch the magic skill and calculate its effects
			skill.activateSkill(caster, item, targets.toArray(new WorldObject[0]));
			
			PlayerInstance player = caster.getActingPlayer();
			if (player != null)
			{
				for (WorldObject obj : targets)
				{
					if (!(obj instanceof Creature))
					{
						continue;
					}
					
					if (skill.isBad())
					{
						if (obj.isPlayable())
						{
							// Update pvpflag.
							player.updatePvPStatus((Creature) obj);
							
							if (obj.isSummon())
							{
								((Summon) obj).updateAndBroadcastStatus(1);
							}
						}
						else if (obj.isAttackable())
						{
							// Add hate to the attackable, and put it in the attack list.
							((Attackable) obj).addDamageHate(caster, 0, -skill.getEffectPoint());
							((Creature) obj).addAttackerToAttackByList(caster);
						}
						
						// notify target AI about the attack
						if (((Creature) obj).hasAI() && !skill.hasEffectType(L2EffectType.HATE))
						{
							((Creature) obj).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
						}
					}
					else if (obj.isMonster() || (obj.isPlayable() && ((obj.getActingPlayer().getPvpFlag() > 0) || (obj.getActingPlayer().getReputation() < 0))))
					{
						// Supporting players or monsters result in pvpflag.
						player.updatePvPStatus();
					}
				}
				
				// Mobs in range 1000 see spell
				World.getInstance().forEachVisibleObjectInRange(player, Npc.class, 1000, npcMob ->
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcSkillSee(npcMob, player, skill, caster.isSummon(), targets.toArray(new WorldObject[0])), npcMob);
					
					// On Skill See logic
					if (npcMob.isAttackable())
					{
						final Attackable attackable = (Attackable) npcMob;
						
						if (skill.getEffectPoint() > 0)
						{
							if (attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
							{
								WorldObject npcTarget = attackable.getTarget();
								for (WorldObject skillTarget : targets)
								{
									if ((npcTarget == skillTarget) || (npcMob == skillTarget))
									{
										Creature originalCaster = caster.isSummon() ? caster : player;
										attackable.addDamageHate(originalCaster, 0, (skill.getEffectPoint() * 150) / (attackable.getLevel() + 7));
									}
								}
							}
						}
					}
				});
			}
		}
		catch (Exception e)
		{
			LOGGER.warn(caster + " callSkill() failed.", e);
		}
	}
	
	/**
	 * Stops this casting and cleans all cast parameters.<br>
	 * @param aborted if {@code true}, server will send packets to the player, notifying him that the skill has been aborted.
	 */
	public void stopCasting(boolean aborted)
	{
		// Cancel the task and unset it.
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		
		final Creature caster = _caster.get();
		final WorldObject target = _target.get();
		if (caster == null)
		{
			return;
		}
		
		caster.removeSkillCaster(_castingType);
		
		if (caster.isChanneling())
		{
			caster.getSkillChannelizer().stopChanneling();
		}
		
		// If aborted, broadcast casting aborted.
		if (aborted)
		{
			caster.broadcastPacket(new MagicSkillCanceld(caster.getObjectId())); // broadcast packet to stop animations client-side
			caster.sendPacket(ActionFailed.get(_castingType)); // send an "action failed" packet to the caster
		}
		
		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		caster.getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		
		// If there is a queued skill, launch it and wipe the queue.
		if (caster.isPlayer())
		{
			final PlayerInstance currPlayer = caster.getActingPlayer();
			final SkillUseHolder queuedSkill = currPlayer.getQueuedSkill();
			
			if (queuedSkill != null)
			{
				ThreadPoolManager.getInstance().executeGeneral(() ->
				{
					currPlayer.setQueuedSkill(null, null, false, false);
					currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.getItem(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				});
				
				return;
			}
		}
		
		// Attack target after skill use
		// TODO: This shouldnt be here. If skill condition fail, you still go autoattack. This doesn't happen if skill is in cooldown though.
		if ((_skill.nextActionIsAttack()) && (target != null) && (target != caster) && target.canBeAttacked())
		{
			if ((caster.getAI().getNextIntention() == null) || (caster.getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO))
			{
				caster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}
	
	public static void triggerCast(Creature activeChar, Creature target, Skill skill)
	{
		triggerCast(activeChar, target, skill, null, true);
	}
	
	public static void triggerCast(Creature activeChar, WorldObject target, Skill skill, ItemInstance item, boolean ignoreTargetType)
	{
		try
		{
			if ((activeChar == null) || (skill == null))
			{
				return;
			}
			
			if (skill.checkCondition(activeChar, target))
			{
				if (activeChar.isSkillDisabled(skill))
				{
					return;
				}
				
				if (skill.getReuseDelay() > 0)
				{
					activeChar.disableSkill(skill, skill.getReuseDelay());
				}
				
				if (!ignoreTargetType)
				{
					WorldObject objTarget = skill.getTarget(activeChar, false, false, false);
					if (objTarget.isCreature())
					{
						target = objTarget;
					}
				}
				
				WorldObject[] targets = skill.getTargetsAffected(activeChar, target).toArray(new WorldObject[0]);
				
				if (!skill.isNotBroadcastable())
				{
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, target, skill.getDisplayId(), skill.getLevel(), 0, 0));
				}
				
				// Launch the magic skill and calculate its effects
				skill.activateSkill(activeChar, item, targets);
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed simultaneous cast: ", e);
		}
	}
	
	/**
	 * @return the skill that is casting.
	 */
	public Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * @return the creature casting the skill.
	 */
	public Creature getCaster()
	{
		return _caster.get();
	}
	
	/**
	 * @return the target this skill is being cast on.
	 */
	public WorldObject getTarget()
	{
		return _target.get();
	}
	
	/**
	 * @return the item that has been used in this casting.
	 */
	public ItemInstance getItem()
	{
		return _item;
	}
	
	/**
	 * @return {@code true} if casting can be aborted through regular means such as cast break while being attacked or while cancelling target, {@code false} otherwise.
	 */
	public boolean canAbortCast()
	{
		return _targets == null; // When targets are allocated, that means skill is already launched, therefore cannot be aborted.
	}
	
	/**
	 * @return the type of this caster, which also defines the casting display bar on the player.
	 */
	public SkillCastingType getCastingType()
	{
		return _castingType;
	}
	
	public boolean isNormalFirstType()
	{
		return _castingType == SkillCastingType.NORMAL;
	}
	
	public boolean isNormalSecondType()
	{
		return _castingType == SkillCastingType.NORMAL_SECOND;
	}
	
	public boolean isAnyNormalType()
	{
		return (_castingType == SkillCastingType.NORMAL) || (_castingType == SkillCastingType.NORMAL_SECOND);
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " [caster: " + String.valueOf(_caster.get()) + " skill: " + String.valueOf(_skill) + " target: " + String.valueOf(_target.get()) + " type: " + String.valueOf(_castingType) + "]";
	}
	
	/**
	 * Checks general conditions for casting a skill through the regular casting type.
	 * @param caster the caster checked if can cast the given skill.
	 * @param skill the skill to be check if it can be casted by the given caster or not.
	 * @return {@code true} if the caster can proceed with casting the given skill, {@code false} otherwise.
	 */
	public static boolean checkUseConditions(Creature caster, Skill skill)
	{
		return checkUseConditions(caster, skill, SkillCastingType.NORMAL);
	}
	
	/**
	 * Checks general conditions for casting a skill.
	 * @param caster the caster checked if can cast the given skill.
	 * @param skill the skill to be check if it can be casted by the given caster or not.
	 * @param castingType used to check if caster is currently casting this type of cast.
	 * @return {@code true} if the caster can proceed with casting the given skill, {@code false} otherwise.
	 */
	public static boolean checkUseConditions(Creature caster, Skill skill, SkillCastingType castingType)
	{
		if (caster == null)
		{
			return false;
		}
		
		if ((skill == null) || caster.isSkillDisabled(skill) || (((skill.getFlyRadius() > 0) || (skill.getFlyType() != null)) && caster.isMovementDisabled()))
		{
			caster.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnCreatureSkillUse(caster, skill, skill.isWithoutAction()), caster, TerminateReturn.class);
		if ((term != null) && term.terminate())
		{
			caster.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if creature is already casting
		if ((castingType != null) && caster.isCastingNow(castingType))
		{
			caster.sendPacket(ActionFailed.get(castingType));
			return false;
		}
		
		// Check if the caster has enough MP
		if (caster.getCurrentMp() < (caster.getStat().getMpConsume(skill) + caster.getStat().getMpInitialConsume(skill)))
		{
			caster.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			caster.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough HP
		if (caster.getCurrentHp() <= skill.getHpConsume())
		{
			caster.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			caster.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Skill mute checks.
		if (!skill.isStatic())
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic())
			{
				if (caster.isMuted())
				{
					caster.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (caster.isPhysicalMuted()) // Check if the skill is physical and if the L2Character is not physical_muted
			{
				caster.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Check if the caster's weapon is limited to use only its own skills
		final Weapon weapon = caster.getActiveWeaponItem();
		if ((weapon != null) && weapon.useWeaponSkillsOnly() && !caster.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			final List<ItemSkillHolder> weaponSkills = weapon.getSkills(ItemSkillType.NORMAL);
			if ((weaponSkills != null) && !weaponSkills.stream().anyMatch(sh -> sh.getSkillId() == skill.getId()))
			{
				caster.sendPacket(SystemMessageId.THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPON_S_SKILL);
				return false;
			}
		}
		
		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if ((skill.getItemConsumeId() > 0) && (skill.getItemConsumeCount() > 0) && (caster.getInventory() != null))
		{
			// Get the L2ItemInstance consumed by the spell
			final ItemInstance requiredItems = caster.getInventory().getItemByItemId(skill.getItemConsumeId());
			if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsumeCount()))
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.hasEffectType(L2EffectType.SUMMON))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_A_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addInt(skill.getItemConsumeCount());
					caster.sendPacket(sm);
				}
				else
				{
					caster.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				}
				return false;
			}
		}
		
		if (caster.isPlayer())
		{
			final PlayerInstance player = caster.getActingPlayer();
			if (player.inObserverMode())
			{
				return false;
			}
			
			if (player.isInOlympiadMode() && skill.isBlockedInOlympiad())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_SKILL_IN_A_OLYMPIAD_MATCH);
				return false;
			}
			
			if (player.isInsideZone(ZoneId.SAYUNE))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILLS_IN_THE_CORRESPONDING_REGION);
				return false;
			}
			
			// Check if not in AirShip
			if (player.isInAirShip() && !skill.hasEffectType(L2EffectType.REFUEL_AIRSHIP))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				return false;
			}
		}
		
		return true;
	}
}
