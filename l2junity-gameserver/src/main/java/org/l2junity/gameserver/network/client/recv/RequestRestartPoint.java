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
package org.l2junity.gameserver.network.client.recv;

import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.data.xml.impl.ClanHallData;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.FortManager;
import org.l2junity.gameserver.instancemanager.MapRegionManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.SiegeClan;
import org.l2junity.gameserver.model.TeleportWhereType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Castle;
import org.l2junity.gameserver.model.entity.ClanHall;
import org.l2junity.gameserver.model.entity.Fort;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.residences.AbstractResidence;
import org.l2junity.gameserver.model.residences.ResidenceFunctionType;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestartPoint implements IClientIncomingPacket
{
	protected int _requestedPointType;
	protected boolean _continuation;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_requestedPointType = packet.readD();
		return true;
	}
	
	class DeathTask implements Runnable
	{
		final PlayerInstance activeChar;
		
		DeathTask(PlayerInstance _activeChar)
		{
			activeChar = _activeChar;
		}
		
		@Override
		public void run()
		{
			portPlayer(activeChar);
		}
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.canRevive())
		{
			return;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(true);
			return;
		}
		else if (!activeChar.isDead())
		{
			_log.warn("Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
			return;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if ((castle != null) && castle.getSiege().isInProgress())
		{
			if ((activeChar.getClan() != null) && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				if (castle.getSiege().getAttackerRespawnDelay() > 0)
				{
					activeChar.sendMessage("You will be re-spawned in " + (castle.getSiege().getAttackerRespawnDelay() / 1000) + " seconds");
				}
				return;
			}
		}
		
		portPlayer(activeChar);
	}
	
	protected final void portPlayer(final PlayerInstance activeChar)
	{
		Location loc = null;
		Instance instance = null;
		
		// force jail
		if (activeChar.isJailed())
		{
			_requestedPointType = 27;
		}
		
		switch (_requestedPointType)
		{
			case 1: // to clanhall
			{
				if ((activeChar.getClan() == null) || (activeChar.getClan().getHideoutId() == 0))
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);
				final ClanHall residense = ClanHallData.getInstance().getClanHallByClan(activeChar.getClan());
				
				if ((residense != null) && (residense.hasFunction(ResidenceFunctionType.EXP_RESTORE)))
				{
					activeChar.restoreExp(residense.getFunction(ResidenceFunctionType.EXP_RESTORE).getValue());
				}
				break;
			}
			case 2: // to castle
			{
				final Castle castle = CastleManager.getInstance().getCastle(activeChar);
				if ((castle != null) && castle.getSiege().isInProgress())
				{
					// Siege in progress
					if (castle.getSiege().checkIsDefender(activeChar.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
					}
					else if (castle.getSiege().checkIsAttacker(activeChar.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
					}
					else
					{
						_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
						return;
					}
				}
				else
				{
					if ((activeChar.getClan() == null) || (activeChar.getClan().getCastleId() == 0))
					{
						return;
					}
					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
				}
				
				if ((castle != null) && (castle.hasFunction(ResidenceFunctionType.EXP_RESTORE)))
				{
					activeChar.restoreExp(castle.getFunction(ResidenceFunctionType.EXP_RESTORE).getValue());
				}
				break;
			}
			case 3: // to fortress
			{
				if ((activeChar.getClan() == null) || (activeChar.getClan().getFortId() == 0))
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.FORTRESS);
				
				final AbstractResidence residense = FortManager.getInstance().getFortByOwner(activeChar.getClan());
				if ((residense != null) && (residense.hasFunction(ResidenceFunctionType.EXP_RESTORE)))
				{
					activeChar.restoreExp(residense.getFunction(ResidenceFunctionType.EXP_RESTORE).getValue());
				}
				break;
			}
			case 4: // to siege HQ
			{
				SiegeClan siegeClan = null;
				final Castle castle = CastleManager.getInstance().getCastle(activeChar);
				final Fort fort = FortManager.getInstance().getFort(activeChar);
				
				if ((castle != null) && castle.getSiege().isInProgress())
				{
					siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
				}
				else if ((fort != null) && fort.getSiege().isInProgress())
				{
					siegeClan = fort.getSiege().getAttackerClan(activeChar.getClan());
				}
				
				if (((siegeClan == null) || siegeClan.getFlag().isEmpty()))
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGEFLAG);
				break;
			}
			case 5: // Fixed or Player is a festival participant
			{
				if (!activeChar.isGM() && !activeChar.getInventory().haveItemForSelfResurrection())
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
					return;
				}
				if (activeChar.isGM() || activeChar.destroyItemByItemId("Feather", 10649, 1, activeChar, false) || activeChar.destroyItemByItemId("Feather", 13300, 1, activeChar, false) || activeChar.destroyItemByItemId("Feather", 13128, 1, activeChar, false))
				{
					activeChar.doRevive(100.00);
				}
				else
				{
					instance = activeChar.getInstanceWorld();
					loc = new Location(activeChar);
				}
				break;
			}
			case 6: // TODO: Agathon resurrection
			{
				break;
			}
			case 7: // TODO: Adventurer's Song
			{
				break;
			}
			case 27: // to jail
			{
				if (!activeChar.isJailed())
				{
					return;
				}
				loc = new Location(-114356, -249645, -2984);
				break;
			}
			default:
			{
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
				break;
			}
		}
		
		// Teleport and revive
		if (loc != null)
		{
			activeChar.setIsPendingRevive(true);
			activeChar.teleToLocation(loc, true, instance);
		}
	}
	
}
