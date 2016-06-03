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
import org.l2junity.gameserver.data.xml.impl.AbilityPointsData;
import org.l2junity.gameserver.enums.UserInfoType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.ability.ExAcquireAPSkillList;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestChangeAbilityPoint implements IClientIncomingPacket
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
		
		if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		
		if (activeChar.getAbilityPoints() >= Config.ABILITY_MAX_POINTS)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ACQUIRE_ANY_MORE_ABILITY_POINTS);
			return;
		}
		
		if (activeChar.isInOlympiadMode() || activeChar.isOnEvent(CeremonyOfChaosEvent.class))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		
		long spRequired = AbilityPointsData.getInstance().getPrice(activeChar.getAbilityPoints());
		if (spRequired > activeChar.getSp())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NEED_S1_SP_TO_CONVERT_TO1_ABILITY_POINT);
			sm.addLong(spRequired);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.getStat().removeSp(spRequired))
		{
			activeChar.setAbilityPoints(activeChar.getAbilityPoints() + 1);
			final UserInfo info = new UserInfo(activeChar, false);
			info.addComponentType(UserInfoType.SLOTS, UserInfoType.CURRENT_HPMPCP_EXP_SP);
			activeChar.sendPacket(info);
			activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
		}
	}
}
