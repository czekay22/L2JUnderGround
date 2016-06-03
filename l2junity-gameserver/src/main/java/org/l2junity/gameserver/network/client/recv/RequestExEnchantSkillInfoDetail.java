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

import org.l2junity.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2junity.gameserver.model.EnchantSkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfoDetail;
import org.l2junity.network.PacketReader;

/**
 * Format (ch) ddd c: (id) 0xD0 h: (subid) 0x31 d: type d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfoDetail implements IClientIncomingPacket
{
	private int _type;
	private int _skillId;
	private int _skillLvl;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_type = packet.readD();
		_skillId = packet.readD();
		_skillLvl = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		PlayerInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		int reqSkillLvl = -2;
		
		if ((_type == 0) || (_type == 1))
		{
			reqSkillLvl = _skillLvl - 1; // enchant
		}
		else if (_type == 2)
		{
			reqSkillLvl = _skillLvl + 1; // untrain
		}
		else if (_type == 3)
		{
			reqSkillLvl = _skillLvl; // change route
		}
		
		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		
		// dont have such skill
		if (playerSkillLvl == -1)
		{
			return;
		}
		
		// if reqlvl is 100,200,.. check base skill lvl enchant
		if ((reqSkillLvl % 100) == 0)
		{
			EnchantSkillLearn esl = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
			if (esl != null)
			{
				// if player dont have min level to enchant
				if (playerSkillLvl != esl.getBaseLevel())
				{
					return;
				}
			}
			// enchant data dont exist?
			else
			{
				return;
			}
		}
		else if (playerSkillLvl != reqSkillLvl)
		{
			// change route is different skill lvl but same enchant
			if ((_type == 3) && ((playerSkillLvl % 100) != (_skillLvl % 100)))
			{
				return;
			}
		}
		
		// send skill enchantment detail
		client.sendPacket(new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, activeChar));
	}
}
