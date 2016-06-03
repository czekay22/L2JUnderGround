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
package org.l2junity.gameserver.network.client.recv.pledgebonus;

import org.l2junity.gameserver.enums.ClanRewardType;
import org.l2junity.gameserver.model.ClanMember;
import org.l2junity.gameserver.model.L2Clan;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.pledge.ClanRewardBonus;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestPledgeBonusReward implements IClientIncomingPacket
{
	private int _type;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_type = packet.readC();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance player = client.getActiveChar();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		if ((_type < 0) || (_type > ClanRewardType.values().length))
		{
			return;
		}
		
		final L2Clan clan = player.getClan();
		final ClanRewardType type = ClanRewardType.values()[_type];
		final ClanMember member = clan.getClanMember(player.getObjectId());
		if (clan.canClaimBonusReward(player, type))
		{
			final ClanRewardBonus bonus = type.getAvailableBonus(player.getClan());
			if (bonus != null)
			{
				final ItemHolder itemReward = bonus.getItemReward();
				final SkillHolder skillReward = bonus.getSkillReward();
				if (itemReward != null)
				{
					player.addItem("ClanReward", itemReward.getId(), itemReward.getCount(), player, true);
				}
				else if (skillReward != null)
				{
					skillReward.getSkill().activateSkill(player, player);
				}
				member.setRewardClaimed(type);
			}
			else
			{
				_log.warn("{} Attempting to claim reward but clan({}) doesn't have such!", player, clan);
			}
		}
	}
}
