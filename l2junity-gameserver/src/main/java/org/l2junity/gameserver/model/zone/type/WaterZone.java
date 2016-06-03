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
package org.l2junity.gameserver.model.zone.type;

import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.network.client.send.NpcInfo;
import org.l2junity.gameserver.network.client.send.ServerObjectInfo;

public class WaterZone extends ZoneType
{
	public WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		// TODO: update to only send speed status when that packet is known
		if (character.isPlayer())
		{
			PlayerInstance player = character.getActingPlayer();
			if (player.checkTransformed(transform -> !transform.canSwim()))
			{
				character.stopTransformation(true);
			}
			else
			{
				player.broadcastUserInfo();
			}
		}
		else if (character.isNpc())
		{
			World.getInstance().forEachVisibleObject(character, PlayerInstance.class, player ->
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				}
				else
				{
					player.sendPacket(new NpcInfo((Npc) character));
				}
			});
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		// TODO: update to only send speed status when that packet is known
		if (character.isPlayer())
		{
			character.getActingPlayer().broadcastUserInfo();
		}
		else if (character.isNpc())
		{
			World.getInstance().forEachVisibleObject(character, PlayerInstance.class, player ->
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				}
				else
				{
					player.sendPacket(new NpcInfo((Npc) character));
				}
			});
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
