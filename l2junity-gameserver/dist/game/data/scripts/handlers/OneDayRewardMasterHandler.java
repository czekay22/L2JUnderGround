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
package handlers;

import org.l2junity.gameserver.handler.OneDayRewardHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handlers.onedayrewardshandlers.BossOneDayRewardHandler;
import handlers.onedayrewardshandlers.CeremonyOfChaosOneDayRewardHandler;
import handlers.onedayrewardshandlers.FishingOneDayRewardHandler;
import handlers.onedayrewardshandlers.LevelOneDayRewardHandler;
import handlers.onedayrewardshandlers.OlympiadOneDayRewardHandler;
import handlers.onedayrewardshandlers.QuestOneDayRewardHandler;
import handlers.onedayrewardshandlers.SiegeOneDayRewardHandler;

/**
 * @author UnAfraid
 */
public class OneDayRewardMasterHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OneDayRewardMasterHandler.class);
	
	public static void main(String[] args)
	{
		OneDayRewardHandler.getInstance().registerHandler("level", LevelOneDayRewardHandler::new);
		// OneDayRewardHandler.getInstance().registerHandler("loginAllWeek", LoginAllWeekOneDayRewardHandler::new);
		// OneDayRewardHandler.getInstance().registerHandler("loginAllMonth", LoginAllWeekOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("quest", QuestOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("olympiad", OlympiadOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("siege", SiegeOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("ceremonyofchaos", CeremonyOfChaosOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("boss", BossOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("fishing", FishingOneDayRewardHandler::new);
		LOGGER.info("Loaded {} handlers.", OneDayRewardHandler.getInstance().size());
	}
}
