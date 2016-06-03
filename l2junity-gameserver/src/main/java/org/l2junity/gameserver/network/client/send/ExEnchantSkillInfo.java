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
package org.l2junity.gameserver.network.client.send;

import java.util.LinkedList;
import java.util.List;

import org.l2junity.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2junity.gameserver.model.EnchantSkillGroup.EnchantSkillHolder;
import org.l2junity.gameserver.model.EnchantSkillLearn;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ExEnchantSkillInfo implements IClientOutgoingPacket
{
	private final List<Integer> _routes = new LinkedList<>(); // skill lvls for each route
	
	private final int _id;
	private final int _lvl;
	private boolean _maxEnchanted = false;
	
	public ExEnchantSkillInfo(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
		
		EnchantSkillLearn enchantLearn = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (_lvl > 100)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_lvl);
				
				// get detail for next level
				EnchantSkillHolder esd = enchantLearn.getEnchantSkillHolder(_lvl);
				
				// if it exists add it
				if (esd != null)
				{
					_routes.add(_lvl); // current enchant add firts
				}
				
				int skillLvL = (_lvl % 100);
				
				for (int route : enchantLearn.getAllRoutes())
				{
					if (((route * 100) + skillLvL) == _lvl)
					{
						continue;
					}
					// add other levels of all routes - same lvl as enchanted
					// lvl
					_routes.add((route * 100) + skillLvL);
				}
				
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add((route * 100) + 1);
				}
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_INFO.writeId(packet);
		
		packet.writeD(_id);
		packet.writeD(_lvl);
		packet.writeD(_maxEnchanted ? 0 : 1);
		packet.writeD(_lvl > 100 ? 1 : 0); // enchanted?
		packet.writeD(_routes.size());
		_routes.forEach(packet::writeD);
		return true;
	}
}
