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
package org.l2junity.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Map;

import org.l2junity.gameserver.data.xml.impl.ClanHallData;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.clanhallauction.ClanHallAuction;
import org.l2junity.gameserver.model.eventengine.AbstractEvent;
import org.l2junity.gameserver.model.eventengine.AbstractEventManager;
import org.l2junity.gameserver.model.eventengine.ScheduleTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sdw
 */
public class ClanHallAuctionManager extends AbstractEventManager<AbstractEvent<?>>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ClanHallAuctionManager.class);
	
	private static final Map<Integer, ClanHallAuction> AUCTIONS = new HashMap<>();
	
	protected ClanHallAuctionManager()
	{
	}
	
	@ScheduleTarget
	private void onEventStart()
	{
		LOGGER.info("Clan Hall Auction has started!");
		AUCTIONS.clear();
		
		//@formatter:off
		ClanHallData.getInstance().getFreeAuctionableHall()
			.forEach(c -> AUCTIONS.put(c.getResidenceId(), new ClanHallAuction(c.getResidenceId())));
		//@formatter:on
	}
	
	@ScheduleTarget
	private void onEventEnd()
	{
		AUCTIONS.values().forEach(ClanHallAuction::finalizeAuctions);
		AUCTIONS.clear();
		LOGGER.info("Clan Hall Auction has ended!");
	}
	
	@Override
	public void onInitialized()
	{
	}
	
	public ClanHallAuction getClanHallAuctionById(int clanHallId)
	{
		return AUCTIONS.get(clanHallId);
	}
	
	public ClanHallAuction getClanHallAuctionByClan(L2Clan clan)
	{
		//@formatter:off
		return AUCTIONS.values().stream()
			.filter(a -> a.getBids().containsKey(clan.getId()))
			.findFirst()
			.orElse(null);
		//@formatter:on
	}
	
	public boolean checkForClanBid(int clanHallId, L2Clan clan)
	{
		//@formatter:off
		return AUCTIONS.entrySet().stream()
			.filter(a -> a.getKey() != clanHallId)
			.anyMatch(a -> a.getValue().getBids().containsKey(clan.getId()));
		//@formatter:on
	}
	
	public static final ClanHallAuctionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallAuctionManager INSTANCE = new ClanHallAuctionManager();
	}
}
