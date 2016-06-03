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
package handlers.targethandlers;

import org.l2junity.gameserver.GeoData;
import org.l2junity.gameserver.handler.ITargetTypeHandler;
import org.l2junity.gameserver.instancemanager.ZoneManager;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.targets.TargetType;
import org.l2junity.gameserver.model.zone.ZoneRegion;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Target ground location. Returns yourself if your current skill's ground location meets the conditions.
 * @author Nik
 */
public class Ground implements ITargetTypeHandler
{
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.GROUND;
	}
	
	@Override
	public WorldObject getTarget(Creature activeChar, WorldObject selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		if (activeChar.isPlayer())
		{
			Location worldPosition = activeChar.getActingPlayer().getCurrentSkillWorldPosition();
			if (worldPosition != null)
			{
				if (dontMove && !activeChar.isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + activeChar.getTemplate().getCollisionRadius(), false, false))
				{
					return null;
				}
				
				if (!GeoData.getInstance().canSeeTarget(activeChar, worldPosition))
				{
					if (sendMessage)
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
					}
					return null;
				}
				
				final ZoneRegion zoneRegion = ZoneManager.getInstance().getRegion(activeChar);
				if (skill.isBad() && !zoneRegion.checkEffectRangeInsidePeaceZone(skill, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()))
				{
					if (sendMessage)
					{
						activeChar.sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
					}
					return null;
				}
				
				return activeChar; // Return yourself to know that your ground location is legit.
			}
		}
		
		return null;
	}
}