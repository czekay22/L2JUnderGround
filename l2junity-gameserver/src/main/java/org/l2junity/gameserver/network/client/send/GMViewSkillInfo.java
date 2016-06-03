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

import java.util.Collection;

import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class GMViewSkillInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final Collection<Skill> _skills;
	
	public GMViewSkillInfo(PlayerInstance cha)
	{
		_activeChar = cha;
		_skills = _activeChar.getSkillList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GM_VIEW_SKILL_INFO.writeId(packet);
		
		packet.writeS(_activeChar.getName());
		packet.writeD(_skills.size());
		
		boolean isDisabled = (_activeChar.getClan() != null) && (_activeChar.getClan().getReputationScore() < 0);
		
		for (Skill skill : _skills)
		{
			packet.writeD(skill.isPassive() ? 1 : 0);
			packet.writeH(skill.getDisplayLevel());
			packet.writeH(0x00); // Sub level
			packet.writeD(skill.getDisplayId());
			packet.writeD(0x00);
			packet.writeC(isDisabled && skill.isClanSkill() ? 1 : 0);
			packet.writeC(SkillData.getInstance().isEnchantable(skill.getDisplayId()) ? 1 : 0);
		}
		return true;
	}
}