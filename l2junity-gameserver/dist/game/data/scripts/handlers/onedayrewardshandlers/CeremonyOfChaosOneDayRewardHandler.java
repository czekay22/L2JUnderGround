/*
 * Copyright (C) 2004-2016 L2J Unity
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
package handlers.onedayrewardshandlers;

import org.l2junity.gameserver.enums.OneDayRewardStatus;
import org.l2junity.gameserver.handler.AbstractOneDayRewardHandler;
import org.l2junity.gameserver.model.OneDayRewardDataHolder;
import org.l2junity.gameserver.model.OneDayRewardPlayerEntry;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.Containers;
import org.l2junity.gameserver.model.events.EventType;
import org.l2junity.gameserver.model.events.impl.ceremonyofchaos.OnCeremonyOfChaosMatchResult;
import org.l2junity.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author UnAfraid
 */
public class CeremonyOfChaosOneDayRewardHandler extends AbstractOneDayRewardHandler
{
	private final int _amount;
	
	public CeremonyOfChaosOneDayRewardHandler(OneDayRewardDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_CEREMONY_OF_CHAOS_MATCH_RESULT, (OnCeremonyOfChaosMatchResult event) -> onCeremonyOfChaosMatchResult(event), this));
	}
	
	@Override
	public boolean isAvailable(PlayerInstance player)
	{
		final OneDayRewardPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case NOT_AVAILABLE: // Initial state
				{
					if (entry.getProgress() >= _amount)
					{
						entry.setStatus(OneDayRewardStatus.AVAILABLE);
						storePlayerEntry(entry);
					}
					break;
				}
				case AVAILABLE:
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private void onCeremonyOfChaosMatchResult(OnCeremonyOfChaosMatchResult event)
	{
		event.getMembers().forEach(member ->
		{
			final OneDayRewardPlayerEntry entry = getPlayerEntry(member.getObjectId(), true);
			if (entry.getStatus() == OneDayRewardStatus.NOT_AVAILABLE)
			{
				if (entry.increaseProgress() >= _amount)
				{
					entry.setStatus(OneDayRewardStatus.AVAILABLE);
				}
				storePlayerEntry(entry);
			}
		});
	}
}
