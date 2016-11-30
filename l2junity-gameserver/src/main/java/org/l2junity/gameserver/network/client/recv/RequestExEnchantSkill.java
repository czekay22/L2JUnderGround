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
package org.l2junity.gameserver.network.client.recv;

import org.l2junity.Config;
import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.enums.CategoryType;
import org.l2junity.gameserver.enums.PrivateStoreType;
import org.l2junity.gameserver.enums.SkillEnchantType;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.EnchantSkillHolder;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfo;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfoDetail;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillResult;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public final class RequestExEnchantSkill implements IClientIncomingPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestExEnchantSkill.class);
	private static final Logger LOGGER_ENCHANT = LoggerFactory.getLogger("enchant.skills");
	
	private SkillEnchantType _type;
	private int _skillId;
	private int _skillLvl;
	private int _skillSubLvl;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		final int type = packet.readD();
		if ((type < 0) || (type >= SkillEnchantType.values().length))
		{
			LOGGER.warn("Client: {} send incorrect type {} on packet: {}", client, type, getClass().getSimpleName());
			return false;
		}
		
		_type = SkillEnchantType.values()[type];
		_skillId = packet.readD();
		_skillLvl = packet.readH();
		_skillSubLvl = packet.readH();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0) || (_skillSubLvl < 0))
		{
			return;
		}
		
		final PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInCategory(CategoryType.AWAKEN_GROUP))
		{
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			return;
		}
		
		if (player.isSellingBuffs())
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			return;
		}
		
		Skill skill = player.getKnownSkill(_skillId);
		if (skill == null)
		{
			return;
		}
		
		if (!skill.isEnchantable())
		{
			return;
		}
		
		if (skill.getLevel() != _skillLvl)
		{
			return;
		}
		
		if (skill.getSubLevel() > 0)
		{
			if (_type == SkillEnchantType.CHANGE)
			{
				final int group1 = (_skillSubLvl % 1000);
				final int group2 = (skill.getSubLevel() % 1000);
				if (group1 != group2)
				{
					LOGGER.warn("{}: Client: {} send incorrect sub level group: {} expected: {}", getClass().getSimpleName(), client, group1, group2);
					return;
				}
			}
			else if ((skill.getSubLevel() + 1) != _skillSubLvl)
			{
				LOGGER.warn("{}: Client: {} send incorrect sub level: {} expected: {}", getClass().getSimpleName(), client, _skillSubLvl, skill.getSubLevel() + 1);
				return;
			}
		}
		
		final EnchantSkillHolder enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(_skillSubLvl % 1000);
		
		// Verify if player has all the ingredients
		for (ItemHolder holder : enchantSkillHolder.getRequiredItems(_type))
		{
			if (player.getInventory().getInventoryItemCount(holder.getId(), 0) < holder.getCount())
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
		}
		
		// Consume all ingredients
		for (ItemHolder holder : enchantSkillHolder.getRequiredItems(_type))
		{
			if (!player.destroyItemByItemId("Skill enchanting", holder.getId(), holder.getCount(), player, true))
			{
				return;
			}
		}
		
		if (player.getSp() < enchantSkillHolder.getSp(_type))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		player.getStat().removeExpAndSp(0, enchantSkillHolder.getSp(_type), false);
		
		switch (_type)
		{
			case BLESSED:
			case NORMAL:
			case IMMORTAL:
			{
				if (Rnd.get(100) <= enchantSkillHolder.getChance(_type))
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLvl, _skillSubLvl);
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.info("Success, Character:{} [{}] Account:{} IP:{}, +{} {} - {} ({}), {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), enchantedSkill.getLevel(), enchantedSkill.getSubLevel(), enchantedSkill.getName(), enchantedSkill.getId(), enchantSkillHolder.getChance(_type));
					}
					player.addSkill(enchantedSkill, true);
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_TRUE);
				}
				else
				{
					final int newSubLevel = skill.getSubLevel() > 0 ? ((skill.getSubLevel() - (skill.getSubLevel() % 1000)) + enchantSkillHolder.getEnchantFailLevel()) : 0;
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLvl, _type == SkillEnchantType.NORMAL ? newSubLevel : skill.getSubLevel());
					if (_type == SkillEnchantType.NORMAL)
					{
						player.addSkill(enchantedSkill, true);
						player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					}
					else if (_type == SkillEnchantType.BLESSED)
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_WILL_REMAIN_UNCHANGED).addSkillName(skill));
					}
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_FALSE);
					
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.info("Failed, Character:{} [{}] Account:{} IP:{}, +{} {} - {} ({}), {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), enchantedSkill.getLevel(), enchantedSkill.getSubLevel(), enchantedSkill.getName(), enchantedSkill.getId(), enchantSkillHolder.getChance(_type));
					}
				}
				break;
			}
			case CHANGE:
			{
				if (Rnd.get(100) <= enchantSkillHolder.getChance(_type))
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLvl, _skillSubLvl);
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.info("Success, Character:{} [{}] Account:{} IP:{}, +{} {} - {} ({}), {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), enchantedSkill.getLevel(), enchantedSkill.getSubLevel(), enchantedSkill.getName(), enchantedSkill.getId(), enchantSkillHolder.getChance(_type));
					}
					player.addSkill(enchantedSkill, true);
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_WILL_REMAIN);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_TRUE);
				}
				else
				{
					final Skill enchantedSkill = SkillData.getInstance().getSkill(_skillId, _skillLvl, enchantSkillHolder.getEnchantFailLevel());
					player.addSkill(enchantedSkill, true);
					player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_FALSE);
					
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LOGGER_ENCHANT.info("Failed, Character:{} [{}] Account:{} IP:{}, +{} {} - {} ({}), {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), enchantedSkill.getLevel(), enchantedSkill.getSubLevel(), enchantedSkill.getName(), enchantedSkill.getId(), enchantSkillHolder.getChance(_type));
					}
				}
				break;
			}
		}
		
		player.broadcastUserInfo();
		player.sendSkillList();
		
		skill = player.getKnownSkill(_skillId);
		player.sendPacket(new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), skill.getSubLevel(), skill.getSubLevel()));
		player.sendPacket(new ExEnchantSkillInfoDetail(_type, skill.getId(), skill.getLevel(), Math.min(skill.getSubLevel() + 1, EnchantSkillGroupsData.MAX_ENCHANT_LEVEL), player));
		player.updateShortCuts(skill.getLevel(), skill.getSubLevel(), skill.getSubLevel());
	}
}
