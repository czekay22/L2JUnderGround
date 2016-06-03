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

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.Henna;
import org.l2junity.gameserver.model.stats.BaseStats;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author Zoey76
 */
public class HennaItemDrawInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _activeChar;
	private final Henna _henna;
	
	public HennaItemDrawInfo(Henna henna, PlayerInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_ITEM_INFO.writeId(packet);
		
		packet.writeD(_henna.getDyeId()); // symbol Id
		packet.writeD(_henna.getDyeItemId()); // item id of dye
		packet.writeQ(_henna.getWearCount()); // total amount of dye require
		packet.writeQ(_henna.getWearFee()); // total amount of Adena require to draw symbol
		packet.writeD(_henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00); // able to draw or not 0 is false and 1 is true
		packet.writeQ(_activeChar.getAdena());
		packet.writeD(_activeChar.getINT()); // current INT
		packet.writeD(_activeChar.getINT() + _activeChar.getHennaValue(BaseStats.INT)); // equip INT
		packet.writeD(_activeChar.getSTR()); // current STR
		packet.writeD(_activeChar.getSTR() + _activeChar.getHennaValue(BaseStats.STR)); // equip STR
		packet.writeD(_activeChar.getCON()); // current CON
		packet.writeD(_activeChar.getCON() + _activeChar.getHennaValue(BaseStats.CON)); // equip CON
		packet.writeD(_activeChar.getMEN()); // current MEN
		packet.writeD(_activeChar.getMEN() + _activeChar.getHennaValue(BaseStats.MEN)); // equip MEN
		packet.writeD(_activeChar.getDEX()); // current DEX
		packet.writeD(_activeChar.getDEX() + _activeChar.getHennaValue(BaseStats.DEX)); // equip DEX
		packet.writeD(_activeChar.getWIT()); // current WIT
		packet.writeD(_activeChar.getWIT() + _activeChar.getHennaValue(BaseStats.WIT)); // equip WIT
		packet.writeD(_activeChar.getLUC()); // current LUC
		packet.writeD(_activeChar.getLUC() + _activeChar.getHennaValue(BaseStats.LUC)); // equip LUC
		packet.writeD(_activeChar.getCHA()); // current CHA
		packet.writeD(_activeChar.getCHA() + _activeChar.getHennaValue(BaseStats.CHA)); // equip CHA
		packet.writeD(0x00); // TODO: Find me!
		return true;
	}
}
