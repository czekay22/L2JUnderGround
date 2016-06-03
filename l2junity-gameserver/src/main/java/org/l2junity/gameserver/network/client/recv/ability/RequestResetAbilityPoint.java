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
package org.l2junity.gameserver.network.client.recv.ability;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.ability.ExAcquireAPSkillList;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestResetAbilityPoint implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isSubClassActive() && !activeChar.isDualClassActive())
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || (activeChar.getActiveRequester() != null))
		{
			return;
		}
		else if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
		{
			client.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		else if (activeChar.isInOlympiadMode() || activeChar.isOnEvent(CeremonyOfChaosEvent.class))
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		else if (activeChar.getAbilityPoints() == 0)
		{
			activeChar.sendMessage("You don't have ability points to reset!");
			return;
		}
		else if (activeChar.getAbilityPointsUsed() == 0)
		{
			activeChar.sendMessage("You haven't used your ability points yet!");
			return;
		}
		else if (activeChar.getAdena() < Config.ABILITY_POINTS_RESET_ADENA)
		{
			client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		if (activeChar.reduceAdena("AbilityPointsReset", Config.ABILITY_POINTS_RESET_ADENA, activeChar, true))
		{
			for (SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
			{
				final Skill skill = activeChar.getKnownSkill(sk.getSkillId());
				if (skill != null)
				{
					activeChar.removeSkill(skill);
				}
			}
			activeChar.setAbilityPointsUsed(0);
			client.sendPacket(new ExAcquireAPSkillList(activeChar));
		}
	}
}
