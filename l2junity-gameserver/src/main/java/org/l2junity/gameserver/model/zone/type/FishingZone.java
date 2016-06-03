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

import java.lang.ref.WeakReference;

import org.l2junity.Config;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.model.Fishing;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.zone.ZoneId;
import org.l2junity.gameserver.model.zone.ZoneType;
import org.l2junity.gameserver.network.client.send.fishing.ExAutoFishAvailable;

/**
 * A fishing zone
 * @author durgus
 */
public class FishingZone extends ZoneType
{
	public FishingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character.isPlayer())
		{
			if ((Config.ALLOWFISHING || character.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)) && !character.isInsideZone(ZoneId.FISHING))
			{
				WeakReference<PlayerInstance> weakPlayer = new WeakReference<>(character.getActingPlayer());
				ThreadPoolManager.getInstance().executeGeneral(new Runnable() {
					@Override
					public void run()
					{
						PlayerInstance player = weakPlayer.get();
						if (player != null)
						{
							Fishing fishing = player.getFishing();
							if (player.isInsideZone(ZoneId.FISHING))
							{
								if (fishing.canFish() && !fishing.isFishing())
								{
									if (fishing.isAtValidLocation())
										player.sendPacket(ExAutoFishAvailable.YES);
									else
										player.sendPacket(ExAutoFishAvailable.NO);
								}
								ThreadPoolManager.getInstance().scheduleGeneral(this, 7000);
							}
							else
							{
								player.sendPacket(ExAutoFishAvailable.NO);
							}
						}
					}
				});
			}
			character.setInsideZone(ZoneId.FISHING, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.FISHING, false);
			character.sendPacket(ExAutoFishAvailable.NO);
		}
	}
	
	/*
	 * getWaterZ() this added function returns the Z value for the water surface. In effect this simply returns the upper Z value of the zone. This required some modification of L2ZoneForm, and zone form extensions.
	 */
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
