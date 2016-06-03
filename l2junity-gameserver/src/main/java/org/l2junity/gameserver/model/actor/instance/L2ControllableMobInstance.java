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
package org.l2junity.gameserver.model.actor.instance;

import org.l2junity.gameserver.ai.CharacterAI;
import org.l2junity.gameserver.ai.ControllableMobAI;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * @author littlecrow
 */
public class L2ControllableMobInstance extends L2MonsterInstance
{
	private boolean _isInvul;
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		// force mobs to be aggro
		return 500;
	}
	
	public L2ControllableMobInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2ControllableMobInstance);
	}
	
	@Override
	protected CharacterAI initAI()
	{
		return new ControllableMobAI(this);
	}
	
	@Override
	public void detachAI()
	{
		// do nothing, AI of controllable mobs can't be detached automatically
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		setAI(null);
		return true;
	}
}