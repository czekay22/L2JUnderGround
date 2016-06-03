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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.AbnormalVisualEffect;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Sdw
 */
public class ExUserInfoAbnormalVisualEffect implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	
	public ExUserInfoAbnormalVisualEffect(PlayerInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_ABNORMAL_VISUAL_EFFECT.writeId(packet);
		
		packet.writeD(_activeChar.getObjectId());
		packet.writeD(_activeChar.getTransformationId());
		
		final Set<AbnormalVisualEffect> abnormalVisualEffects = _activeChar.getCurrentAbnormalVisualEffects();
		final boolean isInvisible = _activeChar.isInvisible();
		packet.writeD(abnormalVisualEffects.size() + (isInvisible ? 1 : 0));
		for (AbnormalVisualEffect abnormalVisualEffect : abnormalVisualEffects)
		{
			packet.writeH(abnormalVisualEffect.getClientId());
		}
		if (isInvisible)
		{
			packet.writeH(AbnormalVisualEffect.STEALTH.getClientId());
		}
		return true;
	}
}
