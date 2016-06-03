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
package handlers.actionhandlers;

import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.handler.IActionHandler;
import org.l2junity.gameserver.instancemanager.CastleManager;
import org.l2junity.gameserver.instancemanager.SiegeGuardManager;
import org.l2junity.gameserver.model.ClanPrivilege;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.Castle;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

public class L2ItemInstanceAction implements IActionHandler
{
	@Override
	public boolean action(PlayerInstance activeChar, WorldObject target, boolean interact)
	{
		final Castle castle = CastleManager.getInstance().getCastle(target);
		if ((castle != null) && (SiegeGuardManager.getInstance().getSiegeGuardByItem(castle.getResidenceId(), target.getId()) != null))
		{
			if ((activeChar.getClan() == null) || (castle.getOwnerId() != activeChar.getClanId()) || !activeChar.hasClanPrivilege(ClanPrivilege.CS_MERCENARIES))
			{
				activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING);
				activeChar.setTarget(target);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return false;
			}
		}
		
		if (!activeChar.isFlying())
		{
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, target);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2ItemInstance;
	}
}