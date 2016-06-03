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

import org.l2junity.gameserver.handler.IUserCommandHandler;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.olympiad.Olympiad;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Olympiad Stat user command.
 * @author kamy, Zoey76
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		109
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		int nobleObjId = activeChar.getObjectId();
		final WorldObject target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer() || !target.getActingPlayer().isNoble())
		{
			activeChar.sendPacket(SystemMessageId.THIS_COMMAND_CAN_ONLY_BE_USED_WHEN_THE_TARGET_IS_AN_AWAKENED_NOBLESSE_EXALTED);
			return false;
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FOR_THE_CURRENT_OLYMPIAD_YOU_HAVE_PARTICIPATED_IN_S1_MATCH_ES_AND_HAD_S2_WIN_S_AND_S3_DEFEAT_S_YOU_CURRENTLY_HAVE_S4_OLYMPIAD_POINT_S);
		sm.addInt(Olympiad.getInstance().getCompetitionDone(nobleObjId));
		sm.addInt(Olympiad.getInstance().getCompetitionWon(nobleObjId));
		sm.addInt(Olympiad.getInstance().getCompetitionLost(nobleObjId));
		sm.addInt(Olympiad.getInstance().getNoblePoints((PlayerInstance) target));
		activeChar.sendPacket(sm);
		
		final SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.THE_MATCHES_THIS_WEEK_ARE_ALL_CLASS_BATTLES_THE_NUMBER_OF_MATCHES_THAT_ARE_ALLOWED_TO_PARTICIPATE_IS_S1);
		sm2.addInt(Olympiad.getInstance().getRemainingWeeklyMatches(nobleObjId));
		sm2.addInt(Olympiad.getInstance().getRemainingWeeklyMatchesClassed(nobleObjId));
		sm2.addInt(Olympiad.getInstance().getRemainingWeeklyMatchesNonClassed(nobleObjId));
		sm2.addInt(Olympiad.getInstance().getRemainingWeeklyMatchesTeam(nobleObjId));
		activeChar.sendPacket(sm2);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
