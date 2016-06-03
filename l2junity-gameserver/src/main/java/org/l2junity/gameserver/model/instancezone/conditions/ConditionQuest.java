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
package org.l2junity.gameserver.model.instancezone.conditions;

import org.l2junity.gameserver.instancemanager.QuestManager;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.instancezone.InstanceTemplate;
import org.l2junity.gameserver.model.quest.Quest;
import org.l2junity.gameserver.model.quest.QuestState;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Instance quest condition
 * @author malyelfik
 */
public final class ConditionQuest extends Condition
{
	public ConditionQuest(InstanceTemplate template, StatsSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, onlyLeader, showMessageAndHtml);
		// Set message
		setSystemMessage(SystemMessageId.C1_S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED, (message, player) -> message.addCharName(player));
	}
	
	@Override
	protected boolean test(PlayerInstance player, Npc npc)
	{
		final int id = getParameters().getInt("id");
		final Quest q = QuestManager.getInstance().getQuest(id);
		if (q == null)
		{
			return false;
		}
		
		final QuestState qs = player.getQuestState(q.getName());
		if (qs == null)
		{
			return false;
		}
		
		final int cond = getParameters().getInt("cond", -1);
		return (cond != -1) ? qs.isCond(cond) : true;
	}
}
