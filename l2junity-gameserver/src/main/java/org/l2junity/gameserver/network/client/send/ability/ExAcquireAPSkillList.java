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
package org.l2junity.gameserver.network.client.send.ability;

import java.util.ArrayList;
import java.util.List;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.AbilityPointsData;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExAcquireAPSkillList implements IClientOutgoingPacket
{
	private final int _abilityPoints, _usedAbilityPoints;
	private final long _price;
	private final boolean _enable;
	private final List<Skill> _skills = new ArrayList<>();
	
	public ExAcquireAPSkillList(PlayerInstance activeChar)
	{
		_abilityPoints = activeChar.getAbilityPoints();
		_usedAbilityPoints = activeChar.getAbilityPointsUsed();
		_price = AbilityPointsData.getInstance().getPrice(_abilityPoints);
		for (SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
		{
			final Skill knownSkill = activeChar.getKnownSkill(sk.getSkillId());
			if (knownSkill != null)
			{
				if (knownSkill.getLevel() == sk.getSkillLevel())
				{
					_skills.add(knownSkill);
				}
			}
		}
		_enable = (!activeChar.isSubClassActive() || activeChar.isDualClassActive()) && (activeChar.getLevel() >= 99) && activeChar.isNoble();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ACQUIRE_AP_SKILL_LIST.writeId(packet);
		
		packet.writeD(_enable ? 1 : 0);
		packet.writeQ(Config.ABILITY_POINTS_RESET_ADENA);
		packet.writeQ(_price);
		packet.writeD(Config.ABILITY_MAX_POINTS);
		packet.writeD(_abilityPoints);
		packet.writeD(_usedAbilityPoints);
		packet.writeD(_skills.size());
		for (Skill skill : _skills)
		{
			packet.writeD(skill.getId());
			packet.writeD(skill.getLevel());
		}
		return true;
	}
}