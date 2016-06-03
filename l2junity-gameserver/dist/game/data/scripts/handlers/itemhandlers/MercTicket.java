/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2junity.gameserver.enums.PlayerAction;
import org.l2junity.gameserver.handler.IItemHandler;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.SiegeGuardManager;
import org.l2junity.gameserver.model.ClanPrivilege;
import org.l2junity.gameserver.model.actor.Playable;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Castle;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.ListenerRegisterType;
import org.l2junity.gameserver.model.events.annotations.RegisterEvent;
import org.l2junity.gameserver.model.events.annotations.RegisterType;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerDlgAnswer;
import org.l2junity.gameserver.model.holders.SiegeGuardHolder;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.network.client.send.ConfirmDlg;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

import ai.AbstractNpcAI;

/**
 * Mercenary Ticket Item Handler.
 * @author St3eT
 */
public final class MercTicket extends AbstractNpcAI implements IItemHandler
{
	private final Map<Integer, ItemInstance> _items = new ConcurrentHashMap<>();
	
	public MercTicket()
	{
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final PlayerInstance activeChar = playable.getActingPlayer();
		final Castle castle = CastleManager.getInstance().getCastle(activeChar);
		if ((castle == null) || (activeChar.getClan() == null) || (castle.getOwnerId() != activeChar.getClanId()) || !activeChar.hasClanPrivilege(ClanPrivilege.CS_MERCENARIES))
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES);
			return false;
		}
		
		final int castleId = castle.getResidenceId();
		final SiegeGuardHolder holder = SiegeGuardManager.getInstance().getSiegeGuardByItem(castleId, item.getId());
		if ((holder == null) || (castleId != holder.getCastleId()))
		{
			activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
			return false;
		}
		else if (castle.getSiege().isInProgress())
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
			return false;
		}
		else if (SiegeGuardManager.getInstance().isAtNpcLimit(castleId, item.getId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		
		_items.put(activeChar.getObjectId(), item);
		final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE);
		dlg.addTime(15000);
		dlg.addNpcName(holder.getNpcId());
		activeChar.sendPacket(dlg);
		activeChar.addAction(PlayerAction.MERCENARY_CONFIRM);
		return true;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_DLG_ANSWER)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDlgAnswer(OnPlayerDlgAnswer event)
	{
		final PlayerInstance activeChar = event.getActiveChar();
		if (activeChar.removeAction(PlayerAction.MERCENARY_CONFIRM) && _items.containsKey(activeChar.getObjectId()))
		{
			if (SiegeGuardManager.getInstance().isTooCloseToAnotherTicket(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT);
				return;
			}
			
			if (event.getAnswer() == 1)
			{
				final ItemInstance item = _items.get(activeChar.getObjectId());
				SiegeGuardManager.getInstance().addTicket(item.getId(), activeChar);
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false); // Remove item from char's inventory
			}
			_items.remove(activeChar.getObjectId());
		}
	}
}