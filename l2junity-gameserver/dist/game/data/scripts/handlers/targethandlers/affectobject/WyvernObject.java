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
package handlers.targethandlers.affectobject;

import org.l2junity.gameserver.data.xml.impl.CategoryData;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.handler.IAffectObjectHandler;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.skills.targets.AffectObject;

/**
 * @author Nik
 */
public class WyvernObject implements IAffectObjectHandler
{
	@Override
	public boolean checkAffectedObject(Creature activeChar, Creature target)
	{
		// TODO Check if this is proper. Not sure if this is the object we are looking for.
		return CategoryData.getInstance().isInCategory(CategoryType.WYVERN_GROUP, target.getId());
	}
	
	@Override
	public Enum<AffectObject> getAffectObjectType()
	{
		return AffectObject.WYVERN_OBJECT;
	}
}
