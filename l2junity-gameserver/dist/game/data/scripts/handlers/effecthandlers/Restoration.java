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
package handlers.effecthandlers;

import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.PetItemList;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Restoration effect implementation.
 * @author Zoey76
 */
public final class Restoration extends AbstractEffect
{
	private final int _itemId;
	private final int _itemCount;
	
	public Restoration(StatsSet params)
	{
		_itemId = params.getInt("itemId", 0);
		_itemCount = params.getInt("itemCount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (!effected.isPlayable())
		{
			return;
		}
		
		if ((_itemId <= 0) || (_itemCount <= 0))
		{
			effected.sendPacket(SystemMessageId.THERE_WAS_NOTHING_FOUND_INSIDE);
			_log.warn(Restoration.class.getSimpleName() + " effect with wrong item Id/count: " + _itemId + "/" + _itemCount + "!");
			return;
		}
		
		if (effected.isPlayer())
		{
			effected.getActingPlayer().addItem("Skill", _itemId, _itemCount, effector, true);
		}
		else if (effected.isPet())
		{
			effected.getInventory().addItem("Skill", _itemId, _itemCount, effected.getActingPlayer(), effector);
			effected.getActingPlayer().sendPacket(new PetItemList(effected.getInventory().getItems()));
		}
	}
}
