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
package org.l2junity.gameserver.enums;

import java.util.function.Function;

import org.l2junity.gameserver.data.xml.impl.ClanRewardData;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.pledge.ClanRewardBonus;

/**
 * @author UnAfraid
 */
public enum ClanRewardType
{
	MEMBERS_ONLINE(0, L2Clan::getPreviousMaxOnlinePlayers),
	HUNTING_MONSTERS(1, L2Clan::getPreviousHuntingPoints);
	
	final int _clientId;
	final int _mask;
	final Function<L2Clan, Integer> _pointsFunction;
	
	ClanRewardType(int clientId, Function<L2Clan, Integer> pointsFunction)
	{
		_clientId = clientId;
		_mask = 1 << clientId;
		_pointsFunction = pointsFunction;
	}
	
	public int getClientId()
	{
		return _clientId;
	}
	
	public int getMask()
	{
		return _mask;
	}
	
	public ClanRewardBonus getAvailableBonus(L2Clan clan)
	{
		ClanRewardBonus availableBonus = null;
		for (ClanRewardBonus bonus : ClanRewardData.getInstance().getClanRewardBonuses(this))
		{
			if (bonus.getRequiredAmount() <= _pointsFunction.apply(clan))
			{
				if ((availableBonus == null) || (availableBonus.getLevel() < bonus.getLevel()))
				{
					availableBonus = bonus;
				}
			}
		}
		return availableBonus;
	}
	
	public static int getDefaultMask()
	{
		int mask = 0;
		for (ClanRewardType type : values())
		{
			mask |= type.getMask();
		}
		return mask;
	}
}
