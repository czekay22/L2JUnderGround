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

import static org.l2junity.gameserver.model.actor.Npc.INTERACTION_DISTANCE;

import java.util.HashSet;
import java.util.Set;

import org.l2junity.Config;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.model.ItemRequest;
import org.l2junity.gameserver.model.TradeList;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreBuy implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 20; // length of the one item
	
	private int _storePlayerId;
	private Set<ItemRequest> _items = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_storePlayerId = packet.readD();
		int count = packet.readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != packet.getReadableBytes()))
		{
			return false;
		}
		_items = new HashSet<>();
		
		for (int i = 0; i < count; i++)
		{
			int objectId = packet.readD();
			long cnt = packet.readQ();
			long price = packet.readQ();
			
			if ((objectId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return false;
			}
			
			_items.add(new ItemRequest(objectId, cnt, price));
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_items == null)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Cannot set private store in Ceremony of Chaos event.
		if (player.isOnEvent(CeremonyOfChaosEvent.class))
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_OR_WORKSHOP_IN_THE_CEREMONY_OF_CHAOS);
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You are buying items too fast.");
			return;
		}
		
		WorldObject object = World.getInstance().getPlayer(_storePlayerId);
		if ((object == null) || player.isCursedWeaponEquipped())
		{
			return;
		}
		
		PlayerInstance storePlayer = (PlayerInstance) object;
		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
		{
			return;
		}
		
		if (player.getInstanceWorld() != storePlayer.getInstanceWorld())
		{
			return;
		}
		
		if (!((storePlayer.getPrivateStoreType() == PrivateStoreType.SELL) || (storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)))
		{
			return;
		}
		
		TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _items.size())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + client.getActiveChar().getName() + " tried to buy less items than sold by package-sell, ban this player for bot usage!";
				Util.handleIllegalPlayerAction(client.getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		int result = storeList.privateStoreBuy(player, _items);
		if (result > 0)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			if (result > 1)
			{
				_log.warn("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			}
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PrivateStoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
	}
}
