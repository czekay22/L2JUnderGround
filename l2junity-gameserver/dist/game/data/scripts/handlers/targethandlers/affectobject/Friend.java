/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.targethandlers.affectobject;

import org.l2junity.gameserver.handler.IAffectObjectHandler;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.targets.AffectObject;
import org.l2junity.gameserver.model.zone.ZoneId;

/**
 * @author Nik
 */
public class Friend implements IAffectObjectHandler
{
	@Override
	public boolean checkAffectedObject(Creature activeChar, Creature target)
	{
		if (activeChar == target)
		{
			return true;
		}
		
		final PlayerInstance player = activeChar.getActingPlayer();
		final PlayerInstance targetPlayer = target.getActingPlayer();
		
		if (player != null)
		{
			if (targetPlayer != null)
			{
				// Same player.
				if (player == targetPlayer)
				{
					return true;
				}
				
				// Party (command channel doesn't make you friends).
				final Party party = player.getParty();
				final Party targetParty = targetPlayer.getParty();
				if ((party != null) && (targetParty != null) && (party.getLeaderObjectId() == targetParty.getLeaderObjectId()))
				{
					return true;
				}
				
				// Arena.
				if (activeChar.isInsideZone(ZoneId.PVP) && target.isInsideZone(ZoneId.PVP))
				{
					return false;
				}
				
				// Duel.
				if (player.isInDuel() && targetPlayer.isInDuel() && (player.getDuelId() == targetPlayer.getDuelId()))
				{
					return false;
				}
				
				// Olympiad.
				if (player.isInOlympiadMode() && targetPlayer.isInOlympiadMode() && (player.getOlympiadGameId() == targetPlayer.getOlympiadGameId()))
				{
					return false;
				}
				
				// Clan.
				final L2Clan clan = player.getClan();
				final L2Clan targetClan = targetPlayer.getClan();
				if (clan != null)
				{
					if (clan == targetClan)
					{
						return true;
					}
					
					// War
					if ((targetClan != null) && clan.isAtWarWith(targetClan) && targetClan.isAtWarWith(clan))
					{
						return false;
					}
				}
				
				// Alliance.
				if ((player.getAllyId() != 0) && (player.getAllyId() == targetPlayer.getAllyId()))
				{
					return true;
				}
				
				// Siege.
				if (target.isInsideZone(ZoneId.SIEGE))
				{
					// Players in the same siege side at the same castle are considered friends.
					if ((player.getSiegeState() > 0) && (player.getSiegeState() == targetPlayer.getSiegeState()) && (player.getSiegeSide() == targetPlayer.getSiegeSide()))
					{
						return true;
					}
					
					return false;
				}
				
				// By default any neutral non-flagged player is considered a friend.
				return (target.getActingPlayer().getPvpFlag() == 0) && (target.getActingPlayer().getReputation() >= 0);
			}
			
			// By default any npc that isnt mob is considered friend.
			return !target.isMonster() && !target.isAutoAttackable(player);
		}
		
		return !target.isAutoAttackable(activeChar);
	}
	
	@Override
	public Enum<AffectObject> getAffectObjectType()
	{
		return AffectObject.FRIEND;
	}
}
