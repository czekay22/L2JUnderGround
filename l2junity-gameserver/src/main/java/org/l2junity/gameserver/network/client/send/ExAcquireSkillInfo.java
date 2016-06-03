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

import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public class ExAcquireSkillInfo implements IClientOutgoingPacket
{
	private final int _id;
	private final int _level;
	private final int _dualClassLevel;
	private final int _spCost;
	private final int _minLevel;
	private final List<ItemHolder> _itemReq;
	private final List<Skill> _skillRem;
	
	/**
	 * Special constructor for Alternate Skill Learning system.<br>
	 * Sets a custom amount of SP.
	 * @param player
	 * @param skillLearn the skill learn.
	 * @param sp the custom SP amount.
	 */
	public ExAcquireSkillInfo(PlayerInstance player, SkillLearn skillLearn, int sp)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_dualClassLevel = skillLearn.getDualClassLevel();
		_spCost = sp;
		_minLevel = skillLearn.getGetLevel();
		_itemReq = skillLearn.getRequiredItems();
		_skillRem = skillLearn.getRemoveSkills().stream().map(player::getKnownSkill).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ACQUIRE_SKILL_INFO.writeId(packet);
		
		packet.writeD(_id);
		packet.writeD(_level);
		packet.writeQ(_spCost);
		packet.writeH(_minLevel);
		packet.writeH(_dualClassLevel);
		packet.writeD(_itemReq.size());
		for (ItemHolder holder : _itemReq)
		{
			packet.writeD(holder.getId());
			packet.writeQ(holder.getCount());
		}
		
		packet.writeD(_skillRem.size());
		for (Skill skill : _skillRem)
		{
			packet.writeD(skill.getId());
			packet.writeD(skill.getLevel());
		}
		return true;
	}
}
