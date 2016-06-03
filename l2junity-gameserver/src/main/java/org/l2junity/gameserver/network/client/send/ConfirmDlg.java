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

import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketWriter;

/**
 * ConfirmDlg server packet implementation.
 * @author kombat, UnAfraid
 */
public class ConfirmDlg extends AbstractMessagePacket<ConfirmDlg>
{
	private int _time;
	private int _requesterId;
	
	public ConfirmDlg(SystemMessageId smId)
	{
		super(smId);
	}
	
	public ConfirmDlg(int id)
	{
		this(SystemMessageId.getSystemMessageId(id));
	}
	
	public ConfirmDlg(String text)
	{
		this(SystemMessageId.S13);
		addString(text);
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}
	
	@Override
	protected void writeParamsSize(PacketWriter packet, int size)
	{
		packet.writeD(size);
	}
	
	@Override
	protected void writeParamType(PacketWriter packet, int type)
	{
		packet.writeD(type);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CONFIRM_DLG.writeId(packet);
		
		packet.writeD(getId());
		writeMe(packet);
		packet.writeD(_time);
		packet.writeD(_requesterId);
		return true;
	}
}
