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
package org.l2junity.gameserver.model.actor.tasks.character;

import org.l2junity.gameserver.ai.CtrlEvent;
import org.l2junity.gameserver.model.actor.Creature;

/**
 * Task dedicated to notify character's AI
 * @author xban1x
 */
public final class NotifyAITask implements Runnable
{
	private final Creature _character;
	private final CtrlEvent _event;
	
	public NotifyAITask(Creature character, CtrlEvent event)
	{
		_character = character;
		_event = event;
	}
	
	@Override
	public void run()
	{
		if (_character != null)
		{
			_character.getAI().notifyEvent(_event, null);
		}
	}
}
