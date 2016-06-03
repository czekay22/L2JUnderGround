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

import org.l2junity.gameserver.enums.ShortcutType;
import org.l2junity.gameserver.model.Shortcut;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ShortCutRegister;
import org.l2junity.network.PacketReader;

public final class RequestShortCutReg implements IClientIncomingPacket
{
	private ShortcutType _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _lvl;
	private int _characterType; // 1 - player, 2 - pet
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		final int typeId = packet.readD();
		_type = ShortcutType.values()[(typeId < 1) || (typeId > 6) ? 0 : typeId];
		final int slot = packet.readD();
		_slot = slot % 12;
		_page = slot / 12;
		_id = packet.readD();
		_lvl = packet.readH();
		packet.readH(); // Sublevel
		_characterType = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if ((client.getActiveChar() == null) || (_page > 10) || (_page < 0))
		{
			return;
		}
		
		final Shortcut sc = new Shortcut(_slot, _page, _type, _id, _lvl, _characterType);
		client.getActiveChar().registerShortCut(sc);
		client.sendPacket(new ShortCutRegister(sc));
	}
}
