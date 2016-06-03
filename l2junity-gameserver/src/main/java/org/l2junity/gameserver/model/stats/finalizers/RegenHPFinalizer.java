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
package org.l2junity.gameserver.model.stats.finalizers;

import java.util.Optional;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.ClanHallData;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.FortManager;
import org.l2junity.gameserver.instancemanager.SiegeManager;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.SiegeClan;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2PetInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Siege;
import org.l2junity.gameserver.model.residences.AbstractResidence;
import org.l2junity.gameserver.model.residences.ResidenceFunction;
import org.l2junity.gameserver.model.residences.ResidenceFunctionType;
import org.l2junity.gameserver.model.stats.BaseStats;
import org.l2junity.gameserver.model.stats.IStatsFunction;
import org.l2junity.gameserver.model.stats.Stats;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.type.CastleZone;
import org.l2junity.gameserver.model.zone.type.ClanHallZone;
import org.l2junity.gameserver.model.zone.type.FortZone;
import org.l2junity.gameserver.model.zone.type.MotherTreeZone;
import org.l2junity.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class RegenHPFinalizer implements IStatsFunction
{
	@Override
	public double calc(Creature creature, Optional<Double> base, Stats stat)
	{
		throwIfPresent(base);
		
		double baseValue = creature.isPlayer() ? creature.getActingPlayer().getTemplate().getBaseHpRegen(creature.getLevel()) : creature.getTemplate().getBaseHpReg();
		baseValue *= creature.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && creature.isChampion())
		{
			baseValue *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}
		
		if (creature.isPlayer())
		{
			PlayerInstance player = creature.getActingPlayer();
			
			double siegeModifier = calcSiegeRegenModifier(player);
			if (siegeModifier > 0)
			{
				baseValue *= siegeModifier;
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && (player.getClan() != null) && (player.getClan().getHideoutId() > 0))
			{
				ClanHallZone zone = ZoneManager.getInstance().getZone(player, ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getResidenceId();
				int clanHallIndex = player.getClan().getHideoutId();
				if ((clanHallIndex > 0) && (clanHallIndex == posChIndex))
				{
					final AbstractResidence residense = ClanHallData.getInstance().getClanHallById(player.getClan().getHideoutId());
					if (residense != null)
					{
						final ResidenceFunction func = residense.getFunction(ResidenceFunctionType.HP_REGEN);
						if (func != null)
						{
							baseValue *= func.getValue();
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.CASTLE) && (player.getClan() != null) && (player.getClan().getCastleId() > 0))
			{
				CastleZone zone = ZoneManager.getInstance().getZone(player, CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getResidenceId();
				int castleIndex = player.getClan().getCastleId();
				if ((castleIndex > 0) && (castleIndex == posCastleIndex))
				{
					final AbstractResidence residense = CastleManager.getInstance().getCastleById(player.getClan().getCastleId());
					if (residense != null)
					{
						final ResidenceFunction func = residense.getFunction(ResidenceFunctionType.HP_REGEN);
						if (func != null)
						{
							baseValue *= func.getValue();
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.FORT) && (player.getClan() != null) && (player.getClan().getFortId() > 0))
			{
				FortZone zone = ZoneManager.getInstance().getZone(player, FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getResidenceId();
				int fortIndex = player.getClan().getFortId();
				if ((fortIndex > 0) && (fortIndex == posFortIndex))
				{
					final AbstractResidence residense = FortManager.getInstance().getFortById(player.getClan().getCastleId());
					if (residense != null)
					{
						final ResidenceFunction func = residense.getFunction(ResidenceFunctionType.HP_REGEN);
						if (func != null)
						{
							baseValue *= func.getValue();
						}
					}
				}
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				final MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				baseValue += hpBonus;
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				baseValue *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				baseValue *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				baseValue *= 0.7; // Running
			}
			
			// Add CON bonus
			baseValue *= creature.getLevelMod() * BaseStats.CON.calcBonus(creature);
		}
		else if (creature.isPet())
		{
			baseValue = ((L2PetInstance) creature).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
		}
		
		return Stats.defaultValue(creature, stat, baseValue);
	}
	
	private static double calcSiegeRegenModifier(PlayerInstance activeChar)
	{
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			return 0;
		}
		
		final Siege siege = SiegeManager.getInstance().getSiege(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if ((siege == null) || !siege.isInProgress())
		{
			return 0;
		}
		
		final SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getId());
		if ((siegeClan == null) || siegeClan.getFlag().isEmpty() || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().stream().findAny().get(), true))
		{
			return 0;
		}
		
		return 1.5; // If all is true, then modifier will be 50% more
	}
}
