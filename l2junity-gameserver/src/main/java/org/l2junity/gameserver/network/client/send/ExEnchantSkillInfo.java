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

import java.util.Set;

import org.l2junity.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public final class ExEnchantSkillInfo implements IClientOutgoingPacket
{
	private final Set<Integer> _routes;
	
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final int _currentSubLevel;
	
	public ExEnchantSkillInfo(int skillId, int skillLevel, int skillSubLevel, int currentSubLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
		_skillSubLevel = skillSubLevel;
		_currentSubLevel = currentSubLevel;
		_routes = EnchantSkillGroupsData.getInstance().getRouteForSkill(_skillId, _skillLevel);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_INFO.writeId(packet);
		packet.writeD(_skillId);
		packet.writeH(_skillLevel);
		packet.writeH(_skillSubLevel);
		packet.writeD((_skillSubLevel % 1000) == EnchantSkillGroupsData.MAX_ENCHANT_LEVEL ? 0 : 1);
		packet.writeD(_skillSubLevel > 1000 ? 1 : 0);
		packet.writeD(_routes.size());
		_routes.forEach(route ->
		{
			final int routeId = route / 1000;
			final int currentRouteId = _skillSubLevel / 1000;
			final int subLevel = _currentSubLevel > 0 ? (route + (_currentSubLevel % 1000)) - 1 : route;
			packet.writeH(_skillLevel);
			packet.writeH(currentRouteId != routeId ? subLevel : subLevel + 1);
		});
		return true;
	}
}
