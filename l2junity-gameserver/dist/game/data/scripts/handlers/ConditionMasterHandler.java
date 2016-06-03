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
package handlers;

import org.l2junity.gameserver.handler.ConditionHandler;

import handlers.conditions.CategoryTypeCondition;
import handlers.conditions.NpcLevelCondition;
import handlers.conditions.PlayerLevelCondition;

/**
 * @author Sdw
 */
public class ConditionMasterHandler
{
	public static void main(String[] args)
	{
		ConditionHandler.getInstance().registerHandler("CategoryType", CategoryTypeCondition::new);
		ConditionHandler.getInstance().registerHandler("NpcLevel", NpcLevelCondition::new);
		ConditionHandler.getInstance().registerHandler("PlayerLevel", PlayerLevelCondition::new);
	}
}
