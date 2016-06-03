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
package instances.JiniaGuildHideout2;

import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.instancezone.Instance;
import org.l2junity.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;

/**
 * Jinia Guild Hideout instance zone.
 * @author Adry_85
 */
public final class JiniaGuildHideout2 extends AbstractInstance
{
	// NPC
	private static final int RAFFORTY = 32020;
	// Misc
	private static final int TEMPLATE_ID = 141;
	
	public JiniaGuildHideout2()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
	}
	
	@Override
	protected void onEnter(PlayerInstance player, Instance instance, boolean firstEnter)
	{
		super.onEnter(player, instance, firstEnter);
		if (firstEnter)
		{
			final QuestState qs = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
			if (qs != null)
			{
				qs.setCond(2, true);
			}
		}
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance talker)
	{
		enterInstance(talker, npc, TEMPLATE_ID);
		return super.onTalk(npc, talker);
	}
	
	public static void main(String[] args)
	{
		new JiniaGuildHideout2();
	}
}
