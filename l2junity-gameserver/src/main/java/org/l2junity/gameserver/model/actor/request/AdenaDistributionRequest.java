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
package org.l2junity.gameserver.model.actor.request;

import java.util.List;

import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.send.adenadistribution.ExDivideAdenaCancel;

/**
 * @author Sdw
 */
public class AdenaDistributionRequest extends AbstractRequest
{
	private final PlayerInstance _distributor;
	private final List<PlayerInstance> _players;
	private final int _adenaObjectId;
	private final long _adenaCount;
	
	public AdenaDistributionRequest(PlayerInstance activeChar, PlayerInstance distributor, List<PlayerInstance> players, int adenaObjectId, long adenaCount)
	{
		super(activeChar);
		_distributor = distributor;
		_adenaObjectId = adenaObjectId;
		_players = players;
		_adenaCount = adenaCount;
	}
	
	public PlayerInstance getDistributor()
	{
		return _distributor;
	}
	
	public List<PlayerInstance> getPlayers()
	{
		return _players;
	}
	
	public int getAdenaObjectId()
	{
		return _adenaObjectId;
	}
	
	public long getAdenaCount()
	{
		return _adenaCount;
	}
	
	@Override
	public boolean isUsing(int objectId)
	{
		return objectId == _adenaObjectId;
	}
	
	@Override
	public void onTimeout()
	{
		super.onTimeout();
		_players.forEach(p ->
		{
			p.removeRequest(AdenaDistributionRequest.class);
			p.sendPacket(ExDivideAdenaCancel.STATIC_PACKET);
		});
	}
}
