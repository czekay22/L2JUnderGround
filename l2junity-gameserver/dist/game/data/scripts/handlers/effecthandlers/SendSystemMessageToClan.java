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
package handlers.effecthandlers;

import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SystemMessage;

/**
 * Item Effect: Increase/decrease PK count permanently.
 * @author Nik
 */
public class SendSystemMessageToClan extends AbstractEffect
{
	private final SystemMessage _message;
	
	public SendSystemMessageToClan(StatsSet params)
	{
		int id = params.getInt("id", 0);
		_message = SystemMessage.getSystemMessage(id);
		
		if (_message == null)
		{
			throw new IllegalArgumentException("SystemMessageId not found for id: " + id);
		}
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		PlayerInstance player = effected.getActingPlayer();
		if ((player == null) || (_message == null))
		{
			return;
		}
		
		final L2Clan clan = player.getClan();
		if (clan != null)
		{
			clan.broadcastToOnlineMembers(_message);
		}
	}
}
