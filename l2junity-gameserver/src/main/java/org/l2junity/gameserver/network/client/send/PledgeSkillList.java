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

import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author -Wooden-
 */
public class PledgeSkillList implements IClientOutgoingPacket
{
	private final Skill[] _skills;
	private final SubPledgeSkill[] _subSkills;
	
	public static class SubPledgeSkill
	{
		int _subType;
		int _skillId;
		int _skillLvl;
		
		public SubPledgeSkill(int subType, int skillId, int skillLvl)
		{
			_subType = subType;
			_skillId = skillId;
			_skillLvl = skillLvl;
		}
	}
	
	public PledgeSkillList(L2Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SKILL_LIST.writeId(packet);
		
		packet.writeD(_skills.length);
		packet.writeD(_subSkills.length); // Squad skill length
		for (Skill sk : _skills)
		{
			packet.writeD(sk.getDisplayId());
			packet.writeH(sk.getDisplayLevel());
			packet.writeH(0x00); // Sub level
		}
		for (SubPledgeSkill sk : _subSkills)
		{
			packet.writeD(sk._subType); // Clan Sub-unit types
			packet.writeD(sk._skillId);
			packet.writeH(sk._skillLvl);
			packet.writeH(0x00); // Sub level
		}
		return true;
	}
}
