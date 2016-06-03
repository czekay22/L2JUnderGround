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
package org.l2junity.gameserver.network.client.send.friend;

import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * Support for "Chat with Friends" dialog. <br />
 * Add new friend or delete.
 * @author JIV
 */
public class L2Friend implements IClientOutgoingPacket
{
	private final boolean _action, _online;
	private final int _objid;
	private final String _name;
	
	/**
	 * @param action - true for adding, false for remove
	 * @param objId
	 */
	public L2Friend(boolean action, int objId)
	{
		_action = action;
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.L2_FRIEND.writeId(packet);
		
		packet.writeD(_action ? 1 : 3); // 1-add 3-remove
		packet.writeD(_objid);
		packet.writeS(_name);
		packet.writeD(_online ? 1 : 0);
		packet.writeD(_online ? _objid : 0);
		return true;
	}
}
