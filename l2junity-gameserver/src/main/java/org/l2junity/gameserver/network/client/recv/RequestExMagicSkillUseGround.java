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
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ValidateLocation;
import org.l2junity.gameserver.util.Broadcast;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * Fromat:(ch) dddddc
 * @author -Wooden-
 */
public final class RequestExMagicSkillUseGround implements IClientIncomingPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
		_skillId = packet.readD();
		_ctrlPressed = packet.readD() != 0;
		_shiftPressed = packet.readC() != 0;
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
		int level = activeChar.getSkillLevel(_skillId);
		if (level <= 0)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the L2Skill template corresponding to the skillID received from the client
		Skill skill = SkillData.getInstance().getSkill(_skillId, level);
		
		// Check the validity of the skill
		if (skill != null)
		{
			activeChar.setCurrentSkillWorldPosition(new Location(_x, _y, _z));
			
			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
			Broadcast.toKnownPlayers(activeChar, new ValidateLocation(activeChar));
			
			activeChar.useMagic(skill, null, _ctrlPressed, _shiftPressed);
		}
		else
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			_log.warn("No skill found with id " + _skillId + " and level " + level + " !!");
		}
	}
}
