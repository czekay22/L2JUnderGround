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
import org.l2junity.gameserver.data.xml.impl.ClanHallData;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.handler.IActionHandler;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.DoorInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.entity.ClanHall;
import org.l2junity.gameserver.model.holders.DoorRequestHolder;
import org.l2junity.gameserver.network.client.send.ConfirmDlg;

public class DoorInstanceAction implements IActionHandler
{
	@Override
	public boolean action(PlayerInstance activeChar, WorldObject target, boolean interact)
	{
		// Check if the L2PcInstance already target the L2NpcInstance
		if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			final DoorInstance door = (DoorInstance) target;
			final ClanHall clanHall = ClanHallData.getInstance().getClanHallByDoorId(door.getId());
			// MyTargetSelected my = new MyTargetSelected(getObjectId(), activeChar.getLevel());
			// activeChar.sendPacket(my);
			if (target.isAutoAttackable(activeChar))
			{
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400)
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if ((activeChar.getClan() != null) && (clanHall != null) && (activeChar.getClanId() == clanHall.getOwnerId()))
			{
				if (!door.isInsideRadius(activeChar, Npc.INTERACTION_DISTANCE, false, false))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					activeChar.addScript(new DoorRequestHolder(door));
					if (!door.isOpen())
					{
						activeChar.sendPacket(new ConfirmDlg(1140));
					}
					else
					{
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
				}
			}
			else if ((activeChar.getClan() != null) && (((DoorInstance) target).getFort() != null) && (activeChar.getClan() == ((DoorInstance) target).getFort().getOwnerClan()) && ((DoorInstance) target).isOpenableBySkill() && !((DoorInstance) target).getFort().getSiege().isInProgress())
			{
				if (!((Creature) target).isInsideRadius(activeChar, Npc.INTERACTION_DISTANCE, false, false))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					activeChar.addScript(new DoorRequestHolder((DoorInstance) target));
					if (!((DoorInstance) target).isOpen())
					{
						activeChar.sendPacket(new ConfirmDlg(1140));
					}
					else
					{
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.DoorInstance;
	}
}
