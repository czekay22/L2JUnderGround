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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.model.TimeStamp;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * Skill Cool Time server packet implementation.
 * @author KenM, Zoey76
 */
public class SkillCoolTime implements IClientOutgoingPacket
{
	private final List<TimeStamp> _skillReuseTimeStamps = new ArrayList<>();
	
	public SkillCoolTime(PlayerInstance player)
	{
		final Map<Integer, TimeStamp> skillReuseTimeStamps = player.getSkillReuseTimeStamps();
		if (skillReuseTimeStamps != null)
		{
			for (TimeStamp ts : skillReuseTimeStamps.values())
			{
				final Skill skill = SkillData.getInstance().getSkill(ts.getSkillId(), ts.getSkillLvl());
				if (ts.hasNotPassed() && !skill.isNotBroadcastable())
				{
					_skillReuseTimeStamps.add(ts);
				}
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SKILL_COOL_TIME.writeId(packet);
		
		packet.writeD(_skillReuseTimeStamps.size());
		for (TimeStamp ts : _skillReuseTimeStamps)
		{
			packet.writeD(ts.getSkillId());
			packet.writeD(0x00); // ?
			packet.writeD((int) ts.getReuse() / 1000);
			packet.writeD((int) ts.getRemaining() / 1000);
		}
		return true;
	}
}
