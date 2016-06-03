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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class AcquireSkillList implements IClientOutgoingPacket
{
	final PlayerInstance _activeChar;
	final List<SkillLearn> _learnable;
	
	public AcquireSkillList(PlayerInstance activeChar)
	{
		_activeChar = activeChar;
		_learnable = SkillTreesData.getInstance().getAvailableSkills(activeChar, activeChar.getClassId(), false, false);
		_learnable.addAll(SkillTreesData.getInstance().getNextAvailableSkills(activeChar, activeChar.getClassId(), false, false));
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ACQUIRE_SKILL_LIST.writeId(packet);
		
		packet.writeH(_learnable.size());
		for (SkillLearn skill : _learnable)
		{
			packet.writeD(skill.getSkillId());
			packet.writeD(skill.getSkillLevel());
			packet.writeQ(skill.getLevelUpSp());
			packet.writeC(skill.getGetLevel());
			packet.writeC(skill.getDualClassLevel());
			packet.writeC(skill.getRequiredItems().size());
			for (ItemHolder item : skill.getRequiredItems())
			{
				packet.writeD(item.getId());
				packet.writeQ(item.getCount());
			}
			
			final List<Skill> skillRem = skill.getRemoveSkills().stream().map(_activeChar::getKnownSkill).filter(Objects::nonNull).collect(Collectors.toList());
			
			packet.writeC(skillRem.size());
			for (Skill skillRemove : skillRem)
			{
				packet.writeD(skillRemove.getId());
				packet.writeD(skillRemove.getLevel());
			}
		}
		return true;
	}
}
