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

import org.l2junity.gameserver.enums.SkillEnchantType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfoDetail;
import org.l2junity.network.PacketReader;

/**
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfoDetail implements IClientIncomingPacket
{
	private SkillEnchantType _type;
	private int _skillId;
	private int _skillLvl;
	private int _skillSubLvl;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_type = SkillEnchantType.values()[packet.readD()];
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
		activeChar.sendPacket(new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, _skillSubLvl, activeChar));
	}
}
