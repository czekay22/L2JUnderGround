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
package org.l2junity.gameserver.network.client.recv;

import java.util.Set;

import org.l2junity.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfo;
import org.l2junity.network.PacketReader;

/**
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfo implements IClientIncomingPacket
{
	private int _skillId;
	private int _skillLvl;
	private int _skillSubLvl;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLvl = packet.readH();
		_skillSubLvl = packet.readH();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0) || (_skillSubLvl < 0))
		{
			return;
		}
		
		final PlayerInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isInCategory(CategoryType.AWAKEN_GROUP))
		{
			return;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl, _skillSubLvl);
		if ((skill == null) || (skill.getId() != _skillId))
		{
			return;
		}
		final Set<Integer> route = EnchantSkillGroupsData.getInstance().getRouteForSkill(_skillId, _skillLvl);
		if (route.isEmpty())
		{
			return;
		}
		
		final Skill playerSkill = activeChar.getKnownSkill(_skillId);
		if ((playerSkill.getLevel() != _skillLvl) || (playerSkill.getSubLevel() != _skillSubLvl))
		{
			return;
		}
		
		client.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl, _skillSubLvl, playerSkill.getSubLevel()));
		// ExEnchantSkillInfoDetail - not really necessary I think
		// client.sendPacket(new ExEnchantSkillInfoDetail(SkillEnchantType.NORMAL, _skillId, _skillLvl, _skillSubLvl, activeChar));
	}
}