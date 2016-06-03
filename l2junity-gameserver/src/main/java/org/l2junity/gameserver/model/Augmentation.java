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
package org.l2junity.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.data.xml.impl.OptionData;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.options.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to store an augmentation and its bonuses.
 * @author durgus, UnAfraid
 */
public final class Augmentation
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Augmentation.class);
	private final List<Options> _options = new ArrayList<>();
	private boolean _active;
	private final int _id;
	
	public Augmentation(int id)
	{
		_id = id;
		_active = false;
		int[] stats = new int[2];
		stats[0] = 0x0000FFFF & id;
		stats[1] = (id >> 16);
		
		for (int stat : stats)
		{
			Options op = OptionData.getInstance().getOptions(stat);
			if (op != null)
			{
				_options.add(op);
			}
			else
			{
				LOGGER.warn(getClass().getSimpleName() + ": Couldn't find option: " + stat);
			}
		}
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getId()
	{
		return _id;
	}
	
	public List<Options> getOptions()
	{
		return _options;
	}
	
	public void applyBonus(PlayerInstance player)
	{
		// make sure the bonuses are not applied twice..
		if (_active)
		{
			return;
		}
		
		for (Options op : _options)
		{
			op.apply(player);
		}
		
		player.getStat().recalculateStats(true);
		_active = true;
	}
	
	public void removeBonus(PlayerInstance player)
	{
		// make sure the bonuses are not removed twice
		if (!_active)
		{
			return;
		}
		
		for (Options op : _options)
		{
			op.remove(player);
		}
		
		player.getStat().recalculateStats(true);
		_active = false;
	}
}
