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

import org.l2junity.gameserver.model.residences.AbstractResidence;
import org.l2junity.gameserver.model.residences.ResidenceFunctionType;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Steuf, UnAfraid
 */
public class AgitDecoInfo implements IClientOutgoingPacket
{
	private final AbstractResidence _residense;
	
	public AgitDecoInfo(AbstractResidence residense)
	{
		_residense = residense;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.AGIT_DECO_INFO.writeId(packet);
		packet.writeD(_residense.getResidenceId());
		for (ResidenceFunctionType type : ResidenceFunctionType.values())
		{
			if (type == ResidenceFunctionType.NONE)
			{
				continue;
			}
			packet.writeC(_residense.hasFunction(type) ? 0x01 : 0x00);
		}
		
		// Unknown
		packet.writeD(0); // TODO: Find me!
		packet.writeD(0); // TODO: Find me!
		packet.writeD(0); // TODO: Find me!
		packet.writeD(0); // TODO: Find me!
		packet.writeD(0); // TODO: Find me!
		return true;
	}
}
