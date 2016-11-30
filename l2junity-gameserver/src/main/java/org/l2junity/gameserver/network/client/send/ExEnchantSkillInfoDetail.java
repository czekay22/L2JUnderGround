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
import org.l2junity.gameserver.enums.SkillEnchantType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.EnchantSkillHolder;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author KenM
 */
public class ExEnchantSkillInfoDetail implements IClientOutgoingPacket
{
	private final SkillEnchantType _type;
	private final int _skillId;
	private final int _skillLvl;
	private final int _skillSubLvl;
	private final EnchantSkillHolder _enchantSkillHolder;
	
	public ExEnchantSkillInfoDetail(SkillEnchantType type, int skillId, int skillLvl, int skillSubLvl, PlayerInstance player)
	{
		_type = type;
		_skillId = skillId;
		_skillLvl = skillLvl;
		_skillSubLvl = skillSubLvl;
		
		_enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(skillSubLvl % 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_INFO_DETAIL.writeId(packet);
		
		packet.writeD(_type.ordinal());
		packet.writeD(_skillId);
		packet.writeH(_skillLvl);
		packet.writeH(_skillSubLvl);
		if (_enchantSkillHolder != null)
		{
			packet.writeQ(_enchantSkillHolder.getSp(_type));
			packet.writeD(_enchantSkillHolder.getChance(_type));
			final Set<ItemHolder> holders = _enchantSkillHolder.getRequiredItems(_type);
			packet.writeD(holders.size());
			holders.forEach(holder ->
			{
				packet.writeD(holder.getId());
				packet.writeD((int) holder.getCount());
			});
		}
		return _enchantSkillHolder != null;
	}
}
