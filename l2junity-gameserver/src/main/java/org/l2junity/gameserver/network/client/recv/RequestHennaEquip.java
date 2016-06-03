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

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.HennaData;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.items.Henna;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * @author Zoey76
 */
public final class RequestHennaEquip implements IClientIncomingPacket
{
	private int _symbolId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_symbolId = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("HennaEquip"))
		{
			return;
		}
		
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			_log.warn("Invalid Henna Id: " + _symbolId + " from player " + activeChar);
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final long _count = activeChar.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
		if (henna.isAllowedClass(activeChar.getClassId()) && (_count >= henna.getWearCount()) && (activeChar.getAdena() >= henna.getWearFee()) && activeChar.addHenna(henna))
		{
			activeChar.destroyItemByItemId("Henna", henna.getDyeItemId(), henna.getWearCount(), activeChar, true);
			activeChar.getInventory().reduceAdena("Henna", henna.getWearFee(), activeChar, activeChar.getLastFolkNPC());
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
			activeChar.sendInventoryUpdate(iu);
			activeChar.sendPacket(SystemMessageId.THE_SYMBOL_HAS_BEEN_ADDED);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THE_SYMBOL_CANNOT_BE_DRAWN);
			if (!activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !henna.isAllowedClass(activeChar.getClassId()))
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
			client.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
