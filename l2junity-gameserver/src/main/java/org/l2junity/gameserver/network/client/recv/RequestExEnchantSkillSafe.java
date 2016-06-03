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
import org.l2junity.gameserver.model.EnchantSkillGroup.EnchantSkillHolder;
import org.l2junity.gameserver.model.EnchantSkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.itemcontainer.Inventory;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfo;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillInfoDetail;
import org.l2junity.gameserver.network.client.send.ExEnchantSkillResult;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.UserInfo;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x32 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillSafe implements IClientIncomingPacket
{
	private static final Logger _logEnchant = LoggerFactory.getLogger("enchant.skills");
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLvl = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		PlayerInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_CORRESPONDING_FUNCTION_WHEN_COMPLETING_THE_THIRD_CLASS_CHANGE);
			return;
		}
		
		if (player.getLevel() < 76)
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION_ON_LEVELS_HIGHER_THAN_LV_76);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_STATE_YOU_CAN_ENHANCE_SKILLS_WHEN_NOT_IN_BATTLE_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMED_IN_BATTLE_ON_A_MOUNT_OR_WHILE_THE_SKILL_IS_ON_COOLDOWN);
			return;
		}
		
		if (player.isSellingBuffs())
		{
			player.sendMessage("You cannot use the skill enchanting function while you selling buffs.");
			return;
		}
		
		Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		int costMultiplier = Config.SAFE_ENCHANT_COST_MULTIPLIER;
		int reqItemId = EnchantSkillGroupsData.SAFE_ENCHANT_BOOK;
		
		EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
		final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		if (beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
		{
			return;
		}
		
		int requiredSp = esd.getSpCost() * costMultiplier;
		int requireditems = esd.getAdenaCost() * costMultiplier;
		int rate = esd.getRate(player);
		
		if (player.getSp() >= requiredSp)
		{
			// No config option for safe enchant book consume
			ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (spb == null) // Haven't spellbook
			{
				client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			if (player.getInventory().getAdena() < requireditems)
			{
				client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			
			check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
			
			if (!check)
			{
				client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			// ok. Destroy ONE copy of the book
			if (Rnd.get(100) <= rate)
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					if (skill.getLevel() > 100)
					{
						_logEnchant.info("Safe Success, Character:{} [{}] Account:{} IP:{}, +{} {}({}), {}({}) [{}], {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), skill.getLevel() % 100, skill.getName(), skill.getId(), spb.getName(), spb.getCount(), spb.getObjectId(), rate);
					}
					else
					{
						_logEnchant.info("Safe Success, Character:{} [{}] Account:{} IP:{}, {}({}), {}({}) [{}], {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), skill.getName(), skill.getId(), spb.getName(), spb.getCount(), spb.getObjectId(), rate);
					}
				}
				
				player.addSkill(skill, true);
				
				client.sendPacket(ExEnchantSkillResult.valueOf(true));
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
				sm.addSkillName(_skillId);
				client.sendPacket(sm);
			}
			else
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					if (skill.getLevel() > 100)
					{
						_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}), {}({}) [{}], {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), skill.getLevel() % 100, skill.getName(), skill.getId(), spb.getName(), spb.getCount(), spb.getObjectId(), rate);
					}
					else
					{
						_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, {}({}), {}({}) [{}], {}({}) [{}], {}", player.getName(), player.getObjectId(), player.getAccountName(), player.getIPAddress(), skill.getName(), skill.getId(), spb.getName(), spb.getCount(), spb.getObjectId(), rate);
					}
				}
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_WILL_REMAIN_UNCHANGED);
				sm.addSkillName(_skillId);
				client.sendPacket(sm);
				client.sendPacket(ExEnchantSkillResult.valueOf(false));
			}
			
			client.sendPacket(new UserInfo(player));
			player.sendSkillList();
			final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			client.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			client.sendPacket(new ExEnchantSkillInfoDetail(1, _skillId, afterEnchantSkillLevel + 1, player));
			player.updateShortCuts(_skillId, afterEnchantSkillLevel);
		}
		else
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL));
		}
	}
}
