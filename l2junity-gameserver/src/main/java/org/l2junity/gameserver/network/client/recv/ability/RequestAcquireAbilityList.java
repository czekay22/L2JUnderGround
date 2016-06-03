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
package org.l2junity.gameserver.network.client.recv.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.SkillHolder;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.recv.IClientIncomingPacket;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.ability.ExAcquireAPSkillList;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;

/**
 * @author UnAfraid
 */
public class RequestAcquireAbilityList implements IClientIncomingPacket
{
	private static final int TREE_SIZE = 3;
	private final Map<Integer, SkillHolder> _skills = new LinkedHashMap<>();
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		packet.readD(); // Total size
		for (int i = 0; i < TREE_SIZE; i++)
		{
			int size = packet.readD();
			for (int j = 0; j < size; j++)
			{
				final SkillHolder holder = new SkillHolder(packet.readD(), packet.readD());
				if (_skills.putIfAbsent(holder.getSkillId(), holder) != null)
				{
					_log.warn("Player {} is trying to send two times one skill {} to learn!", client, holder);
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isSubClassActive() && !activeChar.isDualClassActive())
		{
			return;
		}
		
		if ((activeChar.getAbilityPoints() == 0) || (activeChar.getAbilityPoints() == activeChar.getAbilityPointsUsed()))
		{
			_log.warn("Player {} is trying to learn ability without ability points!", activeChar);
			return;
		}
		
		if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		else if (activeChar.isInOlympiadMode()) // TODO: Add Ceremony of Chaos when done.
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		
		final int[] pointsSpent = new int[TREE_SIZE];
		Arrays.fill(pointsSpent, 0);
		
		final List<SkillLearn> skillsToLearn = new ArrayList<>(_skills.size());
		for (SkillHolder holder : _skills.values())
		{
			final SkillLearn learn = SkillTreesData.getInstance().getAbilitySkill(holder.getSkillId(), holder.getSkillLvl());
			if (learn == null)
			{
				_log.warn("SkillLearn {} ({}) not found!", holder.getSkillId(), holder.getSkillLvl());
				client.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			
			final Skill skill = holder.getSkill();
			if (skill == null)
			{
				_log.warn("Skill {} ({}) not found!", holder.getSkillId(), holder.getSkillLvl());
				client.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			
			if (activeChar.getSkillLevel(skill.getId()) > 0)
			{
				pointsSpent[learn.getTreeId() - 1] += skill.getLevel();
			}
			
			skillsToLearn.add(learn);
		}
		
		// Sort the skills by their tree id -> row -> column
		skillsToLearn.sort(Comparator.comparingInt(SkillLearn::getTreeId).thenComparing(SkillLearn::getRow).thenComparing(SkillLearn::getColumn));
		
		for (SkillLearn learn : skillsToLearn)
		{
			final Skill skill = SkillData.getInstance().getSkill(learn.getSkillId(), learn.getSkillLevel());
			final int points;
			final int knownLevel = activeChar.getSkillLevel(skill.getId());
			if (knownLevel == -1) // player didn't knew it at all!
			{
				points = learn.getSkillLevel();
			}
			else
			{
				points = learn.getSkillLevel() - knownLevel;
			}
			
			// Case 1: Learning skill without having X points spent on the specific tree
			if (learn.getPointsRequired() > pointsSpent[learn.getTreeId() - 1])
			{
				_log.warn("Player {} is trying to learn {} without enough ability points spent!", activeChar, skill);
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Case 2: Learning skill without having its parent
			for (SkillHolder required : learn.getPreReqSkills())
			{
				if (activeChar.getSkillLevel(required.getSkillId()) < required.getSkillLvl())
				{
					_log.warn("Player {} is trying to learn {} without having prerequsite skill: {}!", activeChar, skill, required.getSkill());
					client.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			// Case 3 Learning a skill without having enough points
			if ((activeChar.getAbilityPoints() - activeChar.getAbilityPointsUsed()) < points)
			{
				_log.warn("Player {} is trying to learn ability without ability points!", activeChar);
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			pointsSpent[learn.getTreeId() - 1] += points;
			
			activeChar.addSkill(skill, true);
			activeChar.setAbilityPointsUsed(activeChar.getAbilityPointsUsed() + points);
		}
		activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
	}
}