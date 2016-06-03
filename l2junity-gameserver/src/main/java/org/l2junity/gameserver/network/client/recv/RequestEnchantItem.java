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
import org.l2junity.gameserver.data.xml.impl.EnchantItemData;
import org.l2junity.gameserver.enums.ItemSkillType;
import org.l2junity.gameserver.enums.UserInfoType;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.request.EnchantItemRequest;
import org.l2junity.gameserver.model.items.L2Item;
import org.l2junity.gameserver.model.items.enchant.EnchantResultType;
import org.l2junity.gameserver.model.items.enchant.EnchantScroll;
import org.l2junity.gameserver.model.items.enchant.EnchantSupportItem;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.CommonSkill;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.EnchantResult;
import org.l2junity.gameserver.network.client.send.InventoryUpdate;
import org.l2junity.gameserver.network.client.send.MagicSkillUse;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Util;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestEnchantItem implements IClientIncomingPacket
{
	protected static final Logger _logEnchant = LoggerFactory.getLogger("enchant.items");
	
	private int _objectId;
	private int _supportId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_supportId = packet.readD();
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
		
		final EnchantItemRequest request = activeChar.getRequest(EnchantItemRequest.class);
		if ((request == null) || request.isProcessing())
		{
			return;
		}
		
		request.setEnchantingItem(_objectId);
		request.setProcessing(true);
		
		if (!activeChar.isOnline() || client.isDetached())
		{
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		final ItemInstance item = request.getEnchantingItem();
		final ItemInstance scroll = request.getEnchantingScroll();
		final ItemInstance support = request.getSupportItem();
		if ((item == null) || (scroll == null))
		{
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// template for scroll
		final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
		if (scrollTemplate == null)
		{
			return;
		}
		
		// template for support item, if exist
		EnchantSupportItem supportTemplate = null;
		if (support != null)
		{
			if (support.getObjectId() != _supportId)
			{
				activeChar.removeRequest(request.getClass());
				return;
			}
			supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
		}
		
		// first validation check
		if (!scrollTemplate.isValid(item, supportTemplate))
		{
			client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.removeRequest(request.getClass());
			client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// fast auto-enchant cheat check
		if ((request.getTimestamp() == 0) || ((System.currentTimeMillis() - request.getTimestamp()) < 2000))
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ", Config.DEFAULT_PUNISH);
			activeChar.removeRequest(request.getClass());
			client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// attempting to destroy scroll
		if (activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item) == null)
		{
			client.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT2);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have", Config.DEFAULT_PUNISH);
			activeChar.removeRequest(request.getClass());
			client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// attempting to destroy support if exist
		if (support != null)
		{
			if (activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1, activeChar, item) == null)
			{
				client.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT2);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have", Config.DEFAULT_PUNISH);
				activeChar.removeRequest(request.getClass());
				client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
				return;
			}
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		synchronized (item)
		{
			// last validation check
			if ((item.getOwnerId() != activeChar.getObjectId()) || (item.isEnchantable() == 0))
			{
				client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.removeRequest(request.getClass());
				client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
				return;
			}
			
			final EnchantResultType resultType = scrollTemplate.calculateSuccess(activeChar, item, supportTemplate);
			switch (resultType)
			{
				case ERROR:
				{
					client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
					activeChar.removeRequest(request.getClass());
					client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
					break;
				}
				case SUCCESS:
				{
					final L2Item it = item.getItem();
					// Increase enchant level only if scroll's base template has chance, some armors can success over +20 but they shouldn't have increased.
					if (scrollTemplate.getChance(activeChar, item) > 0)
					{
						item.setEnchantLevel(item.getEnchantLevel() + 1);
						item.updateDatabase();
					}
					client.sendPacket(new EnchantResult(EnchantResult.SUCCESS, item));
					
					if (Config.LOG_ITEM_ENCHANTS)
					{
						if (item.getEnchantLevel() > 0)
						{
							if (support == null)
							{
								_logEnchant.info("Success, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
							}
							else
							{
								_logEnchant.info("Success, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
							}
						}
						else
						{
							if (support == null)
							{
								_logEnchant.info("Success, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
							}
							else
							{
								_logEnchant.info("Success, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
							}
						}
					}
					
					// announce the success
					int minEnchantAnnounce = item.isArmor() ? 6 : 7;
					int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
					if ((item.getEnchantLevel() == minEnchantAnnounce) || (item.getEnchantLevel() == maxEnchantAnnounce))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3);
						sm.addCharName(activeChar);
						sm.addInt(item.getEnchantLevel());
						sm.addItemName(item);
						activeChar.broadcastPacket(sm);
						
						Skill skill = CommonSkill.FIREWORK.getSkill();
						if (skill != null)
						{
							activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
						}
					}
					
					if (item.isArmor() && item.isEquipped())
					{
						it.forEachSkill(ItemSkillType.ON_ENCHANT, holder ->
						{
							// add skills bestowed from +4 armor
							if (item.getEnchantLevel() >= holder.getValue())
							{
								activeChar.addSkill(holder.getSkill(), false);
								activeChar.sendSkillList();
							}
						});
					}
					break;
				}
				case FAILURE:
				{
					if (scrollTemplate.isSafe())
					{
						// safe enchant - remain old value
						client.sendPacket(SystemMessageId.ENCHANT_FAILED_THE_ENCHANT_SKILL_FOR_THE_CORRESPONDING_ITEM_WILL_BE_EXACTLY_RETAINED);
						client.sendPacket(new EnchantResult(EnchantResult.SAFE_FAIL, item));
						
						if (Config.LOG_ITEM_ENCHANTS)
						{
							if (item.getEnchantLevel() > 0)
							{
								if (support == null)
								{
									_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
								}
								else
								{
									_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
								}
							}
							else
							{
								if (support == null)
								{
									_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
								}
								else
								{
									_logEnchant.info("Safe Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
								}
							}
						}
					}
					else
					{
						// unequip item on enchant failure to avoid item skills stack
						if (item.isEquipped())
						{
							if (item.getEnchantLevel() > 0)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
								sm.addInt(item.getEnchantLevel());
								sm.addItemName(item);
								client.sendPacket(sm);
							}
							else
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
								sm.addItemName(item);
								client.sendPacket(sm);
							}
							
							ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
							for (ItemInstance itm : unequiped)
							{
								iu.addModifiedItem(itm);
							}
							
							activeChar.sendInventoryUpdate(iu);
							activeChar.broadcastUserInfo();
						}
						
						if (scrollTemplate.isBlessed())
						{
							// blessed enchant - clear enchant value
							client.sendPacket(SystemMessageId.THE_BLESSED_ENCHANT_FAILED_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
							
							item.setEnchantLevel(0);
							item.updateDatabase();
							client.sendPacket(new EnchantResult(EnchantResult.BLESSED_FAIL, 0, 0));
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								if (item.getEnchantLevel() > 0)
								{
									if (support == null)
									{
										_logEnchant.info("Blessed Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
									}
									else
									{
										_logEnchant.info("Blessed Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
									}
								}
								else
								{
									if (support == null)
									{
										_logEnchant.info("Blessed Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
									}
									else
									{
										_logEnchant.info("Blessed Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
									}
								}
							}
						}
						else
						{
							// enchant failed, destroy item
							int crystalId = item.getItem().getCrystalItemId();
							int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
							if (count < 1)
							{
								count = 1;
							}
							
							if (activeChar.getInventory().destroyItem("Enchant", item, activeChar, null) == null)
							{
								// unable to destroy item, cheater ?
								Util.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
								activeChar.removeRequest(request.getClass());
								client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
								
								if (Config.LOG_ITEM_ENCHANTS)
								{
									if (item.getEnchantLevel() > 0)
									{
										if (support == null)
										{
											_logEnchant.info("Unable to destroy, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
										}
										else
										{
											_logEnchant.info("Unable to destroy, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
										}
									}
									else
									{
										if (support == null)
										{
											_logEnchant.info("Unable to destroy, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
										}
										else
										{
											_logEnchant.info("Unable to destroy, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
										}
									}
								}
								return;
							}
							
							World.getInstance().removeObject(item);
							ItemInstance crystals = null;
							if (crystalId != 0)
							{
								crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, item);
								
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
								sm.addItemName(crystals);
								sm.addLong(count);
								client.sendPacket(sm);
							}
							
							if (!Config.FORCE_INVENTORY_UPDATE)
							{
								if (crystals != null)
								{
									iu.addItem(crystals);
								}
							}
							
							if (crystalId == 0)
							{
								client.sendPacket(new EnchantResult(EnchantResult.NO_CRYSTAL, 0, 0));
							}
							else
							{
								client.sendPacket(new EnchantResult(EnchantResult.FAIL, crystalId, count));
							}
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								if (item.getEnchantLevel() > 0)
								{
									if (support == null)
									{
										_logEnchant.info("Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
									}
									else
									{
										_logEnchant.info("Fail, Character:{} [{}] Account:{} IP:{}, +{} {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getEnchantLevel(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
									}
								}
								else
								{
									if (support == null)
									{
										_logEnchant.info("Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId());
									}
									else
									{
										_logEnchant.info("Fail, Character:{} [{}] Account:{} IP:{}, {}({}) [{}], {}({}) [{}], {}({}) [{}]", activeChar.getName(), activeChar.getObjectId(), activeChar.getAccountName(), activeChar.getIPAddress(), item.getName(), item.getCount(), item.getObjectId(), scroll.getName(), scroll.getCount(), scroll.getObjectId(), support.getName(), support.getCount(), support.getObjectId());
									}
								}
							}
						}
					}
					break;
				}
			}
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				if (scroll.getCount() == 0)
				{
					iu.addRemovedItem(scroll);
				}
				else
				{
					iu.addModifiedItem(scroll);
				}
				
				if (item.getCount() == 0)
				{
					iu.addRemovedItem(item);
				}
				else
				{
					iu.addModifiedItem(item);
				}
				
				if (support != null)
				{
					if (support.getCount() == 0)
					{
						iu.addRemovedItem(support);
					}
					else
					{
						iu.addModifiedItem(support);
					}
				}
				
				activeChar.sendInventoryUpdate(iu);
			}
			else
			{
				activeChar.sendItemList(true);
			}
			
			request.setProcessing(false);
			activeChar.broadcastUserInfo(UserInfoType.ENCHANTLEVEL);
		}
	}
}
