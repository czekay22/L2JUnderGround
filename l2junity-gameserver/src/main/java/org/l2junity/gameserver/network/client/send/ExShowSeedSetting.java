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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.l2junity.gameserver.instancemanager.CastleManorManager;
import org.l2junity.gameserver.model.L2Seed;
import org.l2junity.gameserver.model.SeedProduction;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * @author l3x
 */
public class ExShowSeedSetting implements IClientOutgoingPacket
{
	private final int _manorId;
	private final Set<L2Seed> _seeds;
	private final Map<Integer, SeedProduction> _current = new HashMap<>();
	private final Map<Integer, SeedProduction> _next = new HashMap<>();
	
	public ExShowSeedSetting(int manorId)
	{
		final CastleManorManager manor = CastleManorManager.getInstance();
		_manorId = manorId;
		_seeds = manor.getSeedsForCastle(_manorId);
		for (L2Seed s : _seeds)
		{
			// Current period
			SeedProduction sp = manor.getSeedProduct(manorId, s.getSeedId(), false);
			if (sp != null)
			{
				_current.put(s.getSeedId(), sp);
			}
			// Next period
			sp = manor.getSeedProduct(manorId, s.getSeedId(), true);
			if (sp != null)
			{
				_next.put(s.getSeedId(), sp);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_SEED_SETTING.writeId(packet);
		
		packet.writeD(_manorId); // manor id
		packet.writeD(_seeds.size()); // size
		
		for (L2Seed s : _seeds)
		{
			packet.writeD(s.getSeedId()); // seed id
			packet.writeD(s.getLevel()); // level
			packet.writeC(1);
			packet.writeD(s.getReward(1)); // reward 1 id
			packet.writeC(1);
			packet.writeD(s.getReward(2)); // reward 2 id
			packet.writeD(s.getSeedLimit()); // next sale limit
			packet.writeD(s.getSeedReferencePrice()); // price for castle to produce 1
			packet.writeD(s.getSeedMinPrice()); // min seed price
			packet.writeD(s.getSeedMaxPrice()); // max seed price
			// Current period
			if (_current.containsKey(s.getSeedId()))
			{
				final SeedProduction sp = _current.get(s.getSeedId());
				packet.writeQ(sp.getStartAmount()); // sales
				packet.writeQ(sp.getPrice()); // price
			}
			else
			{
				packet.writeQ(0);
				packet.writeQ(0);
			}
			// Next period
			if (_next.containsKey(s.getSeedId()))
			{
				final SeedProduction sp = _next.get(s.getSeedId());
				packet.writeQ(sp.getStartAmount()); // sales
				packet.writeQ(sp.getPrice()); // price
			}
			else
			{
				packet.writeQ(0);
				packet.writeQ(0);
			}
		}
		_current.clear();
		_next.clear();
		return true;
	}
}