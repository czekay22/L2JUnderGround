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

import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.CommonSkill;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

public final class RequestMagicSkillUse implements IClientIncomingPacket
{
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_magicId = packet.readD(); // Identifier of the used skill
		_ctrlPressed = packet.readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = packet.readC() != 0; // True if Shift pressed
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		// Get the current L2PcInstance of the player
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Get the level of the used skill
		Skill skill = activeChar.getKnownSkill(_magicId);
		if (skill == null)
		{
			// Player doesn't know this skill, maybe it's the display Id.
			skill = activeChar.getCustomSkill(_magicId);
			if (skill == null)
			{
				if ((_magicId == CommonSkill.HAIR_ACCESSORY_SET.getId()) || SkillTreesData.getInstance().isSubClassChangeSkill(_magicId, 1))
				{
					skill = SkillData.getInstance().getSkill(_magicId, 1);
				}
				else
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					_log.warn("Skill Id " + _magicId + " not found in player!");
					return;
				}
			}
		}
		
		// Skill is blocked from player use.
		if (skill.isBlockActionUseSkill())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Avoid Use of Skills in AirShip.
		if (activeChar.isInAirShip())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ACTION_IS_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.useMagic(skill, null, _ctrlPressed, _shiftPressed);
	}
}
