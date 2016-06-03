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

import org.l2junity.Config;
import org.l2junity.gameserver.ai.CharacterAI;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.ai.SiegeGuardAI;
import org.l2junity.gameserver.ai.SpecialSiegeGuardAI;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.FortManager;
import org.l2junity.gameserver.model.actor.Attackable;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.entity.Castle;
import org.l2junity.gameserver.model.entity.Fort;
import org.l2junity.gameserver.network.client.send.ActionFailed;

public class L2DefenderInstance extends Attackable
{
	private Castle _castle = null; // the castle which the instance should defend
	private Fort _fort = null; // the fortress which the instance should defend
	
	public L2DefenderInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2DefenderInstance);
	}
	
	@Override
	protected CharacterAI initAI()
	{
		if (getCastle(10000) != null)
		{
			return new SiegeGuardAI(this);
		}
		return new SpecialSiegeGuardAI(this);
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by all except defenders
		if (!(attacker instanceof Playable))
		{
			return false;
		}
		
		PlayerInstance player = attacker.getActingPlayer();
		
		// Check if siege is in progress
		if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()))
		{
			int activeSiegeId = (_fort != null) ? _fort.getResidenceId() : _castle.getResidenceId();
			
			// Check if player is an enemy of this defender npc
			if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1)) || (player.getSiegeState() == 0)))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
		{
			return;
		}
		if (getSpawn() == null)
		{
			return;
		}
		if (!isInsideRadius(getSpawn(), 40, false, false))
		{
			if (Config.DEBUG)
			{
				_log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		
		if ((_fort == null) && (_castle == null))
		{
			_log.warn("L2DefenderInstance spawned outside of Fortress or Castle zone!" + this);
		}
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(PlayerInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				_log.info("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof L2DefenderInstance))
		{
			if ((damage == 0) && (aggro <= 1) && (attacker.isPlayable()))
			{
				PlayerInstance player = attacker.getActingPlayer();
				// Check if siege is in progress
				if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()))
				{
					int activeSiegeId = (_fort != null) ? _fort.getResidenceId() : _castle.getResidenceId();
					if ((player != null) && (((player.getSiegeState() == 2) && player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1))))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
