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

import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class ExEnchantSkillList implements IClientOutgoingPacket
{
	public enum EnchantSkillType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE_ROUTE,
	}
	
	private final EnchantSkillType _type;
	private final List<Skill> _skills = new LinkedList<>();
	
	public ExEnchantSkillList(EnchantSkillType type)
	{
		_type = type;
	}
	
	public void addSkill(Skill skill)
	{
		_skills.add(skill);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_LIST.writeId(packet);
		
		packet.writeD(_type.ordinal());
		packet.writeD(_skills.size());
		for (Skill skill : _skills)
		{
			packet.writeD(skill.getId());
			packet.writeD(skill.getLevel());
		}
		return true;
	}
}