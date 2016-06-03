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

import java.util.ArrayList;
import java.util.List;

import org.l2junity.gameserver.model.holders.NpcLogListHolder;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.string.NpcStringId;
import org.l2junity.network.PacketWriter;

/**
 * @author UnAfraid
 */
public class ExQuestNpcLogList implements IClientOutgoingPacket
{
	private final int _questId;
	private final List<NpcLogListHolder> _npcLogList = new ArrayList<>();
	
	public ExQuestNpcLogList(int questId)
	{
		_questId = questId;
	}
	
	public void addNpc(int npcId, int count)
	{
		_npcLogList.add(new NpcLogListHolder(npcId, false, count));
	}
	
	public void addNpcString(NpcStringId npcStringId, int count)
	{
		_npcLogList.add(new NpcLogListHolder(npcStringId.getId(), true, count));
	}
	
	public void add(NpcLogListHolder holder)
	{
		_npcLogList.add(holder);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_QUEST_NPC_LOG_LIST.writeId(packet);
		
		packet.writeD(_questId);
		packet.writeC(_npcLogList.size());
		for (NpcLogListHolder holder : _npcLogList)
		{
			packet.writeD(holder.isNpcString() ? holder.getId() : holder.getId() + 1000000);
			packet.writeC(holder.isNpcString() ? 0x01 : 0x00);
			packet.writeD(holder.getCount());
		}
		return true;
	}
}