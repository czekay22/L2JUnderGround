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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.UIData;
import org.l2junity.gameserver.model.ActionKey;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.ConnectionState;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * Request Save Key Mapping client packet.
 * @author mrTJO, Zoey76
 */
public class RequestSaveKeyMapping implements IClientIncomingPacket
{
	private final Map<Integer, List<ActionKey>> _keyMap = new HashMap<>();
	private final Map<Integer, List<Integer>> _catMap = new HashMap<>();
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		int category = 0;
		
		packet.readD(); // Unknown
		packet.readD(); // Unknown
		final int _tabNum = packet.readD();
		for (int i = 0; i < _tabNum; i++)
		{
			int cmd1Size = packet.readC();
			for (int j = 0; j < cmd1Size; j++)
			{
				UIData.addCategory(_catMap, category, packet.readC());
			}
			category++;
			
			int cmd2Size = packet.readC();
			for (int j = 0; j < cmd2Size; j++)
			{
				UIData.addCategory(_catMap, category, packet.readC());
			}
			category++;
			
			int cmdSize = packet.readD();
			for (int j = 0; j < cmdSize; j++)
			{
				int cmd = packet.readD();
				int key = packet.readD();
				int tgKey1 = packet.readD();
				int tgKey2 = packet.readD();
				int show = packet.readD();
				UIData.addKey(_keyMap, i, new ActionKey(i, cmd, key, tgKey1, tgKey2, show));
			}
		}
		packet.readD();
		packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (!Config.STORE_UI_SETTINGS || (player == null) || (client.getConnectionState() != ConnectionState.IN_GAME))
		{
			return;
		}
		player.getUISettings().storeAll(_catMap, _keyMap);
	}
}
