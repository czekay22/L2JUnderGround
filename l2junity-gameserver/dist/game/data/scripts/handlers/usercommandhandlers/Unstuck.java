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
package handlers.usercommandhandlers;

import static org.l2junity.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.handler.IUserCommandHandler;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.model.skills.SkillCaster;
import org.l2junity.gameserver.model.skills.SkillCastingType;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Unstuck user command.
 */
public class Unstuck implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance activeChar)
	{
		if (activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot use this function while you are jailed.");
			return false;
		}
		
		int unstuckTimer = (activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000);
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_SKILL_IN_A_OLYMPIAD_MATCH);
			return false;
		}
		
		if (activeChar.isCastingNow(SkillCaster::isAnyNormalType) || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.inObserverMode() || activeChar.isCombatFlagEquipped())
		{
			return false;
		}
		
		Skill escape = SkillData.getInstance().getSkill(2099, 1); // 5 minutes escape
		Skill GM_escape = SkillData.getInstance().getSkill(2100, 1); // 1 second escape
		if (activeChar.getAccessLevel().isGm())
		{
			if (GM_escape != null)
			{
				activeChar.doCast(GM_escape);
				return true;
			}
			activeChar.sendMessage("You use Escape: 1 second.");
		}
		else if ((Config.UNSTUCK_INTERVAL == 300) && (escape != null))
		{
			activeChar.doCast(escape);
			return true;
		}
		else
		{
			SkillCaster skillCaster = SkillCaster.castSkill(activeChar, activeChar.getTarget(), escape, null, SkillCastingType.NORMAL, false, false, unstuckTimer);
			if (skillCaster == null)
			{
				activeChar.sendPacket(ActionFailed.get(SkillCastingType.NORMAL));
				activeChar.getAI().setIntention(AI_INTENTION_ACTIVE);
				return false;
			}
			
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
			}
			else
			{
				activeChar.sendMessage("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
			}
		}
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
