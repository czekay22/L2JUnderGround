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

import java.util.List;

import org.l2junity.gameserver.model.ActionKey;
import org.l2junity.gameserver.model.UIKeysSettings;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author mrTJO
 */
public class ExUISetting implements IClientOutgoingPacket
{
	private final UIKeysSettings _uiSettings;
	private int buffsize, categories;
	
	public ExUISetting(PlayerInstance player)
	{
		_uiSettings = player.getUISettings();
		calcSize();
	}
	
	private void calcSize()
	{
		int size = 16; // initial header and footer
		int category = 0;
		int numKeyCt = _uiSettings.getKeys().size();
		for (int i = 0; i < numKeyCt; i++)
		{
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				size = size + catElList1.size();
			}
			category++;
			size++;
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				size = size + catElList2.size();
			}
			category++;
			size = size + 4;
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				size = size + (keyElList.size() * 20);
			}
		}
		buffsize = size;
		categories = category;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_UI_SETTING.writeId(packet);
		
		packet.writeD(buffsize);
		packet.writeD(categories);
		
		int category = 0;
		
		int numKeyCt = _uiSettings.getKeys().size();
		packet.writeD(numKeyCt);
		for (int i = 0; i < numKeyCt; i++)
		{
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList1 = _uiSettings.getCategories().get(category);
				packet.writeC(catElList1.size());
				for (int cmd : catElList1)
				{
					packet.writeC(cmd);
				}
			}
			else
			{
				packet.writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getCategories().containsKey(category))
			{
				List<Integer> catElList2 = _uiSettings.getCategories().get(category);
				packet.writeC(catElList2.size());
				for (int cmd : catElList2)
				{
					packet.writeC(cmd);
				}
			}
			else
			{
				packet.writeC(0x00);
			}
			category++;
			
			if (_uiSettings.getKeys().containsKey(i))
			{
				List<ActionKey> keyElList = _uiSettings.getKeys().get(i);
				packet.writeD(keyElList.size());
				for (ActionKey akey : keyElList)
				{
					packet.writeD(akey.getCommandId());
					packet.writeD(akey.getKeyId());
					packet.writeD(akey.getToogleKey1());
					packet.writeD(akey.getToogleKey2());
					packet.writeD(akey.getShowStatus());
				}
			}
			else
			{
				packet.writeD(0x00);
			}
		}
		packet.writeD(0x11);
		packet.writeD(0x10);
		return true;
	}
}
