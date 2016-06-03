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

import org.l2junity.gameserver.data.xml.impl.BuyListData;
import org.l2junity.gameserver.datatables.MerchantPriceConfigTable;
import org.l2junity.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import org.l2junity.gameserver.enums.InstanceType;
import org.l2junity.gameserver.enums.TaxType;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2junity.gameserver.model.buylist.ProductList;
import org.l2junity.gameserver.network.client.send.ActionFailed;
import org.l2junity.gameserver.network.client.send.BuyList;
import org.l2junity.gameserver.network.client.send.ExBuySellList;

/**
 * This class ...
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance
{
	private MerchantPriceConfig _mpc;
	
	public L2MerchantInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2MerchantInstance);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker.isMonster())
		{
			return true;
		}
		
		return super.isAutoAttackable(attacker);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/merchant/" + pom + ".htm";
	}
	
	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc()
	{
		return _mpc;
	}
	
	public final void showBuyWindow(PlayerInstance player, int val)
	{
		showBuyWindow(player, val, true);
	}
	
	public final void showBuyWindow(PlayerInstance player, int val, boolean applyTax)
	{
		final ProductList buyList = BuyListData.getInstance().getBuyList(val);
		if (buyList == null)
		{
			_log.warn("BuyList not found! BuyListId:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!buyList.isNpcAllowed(getId()))
		{
			_log.warn("Npc not allowed in BuyList! BuyListId:" + val + " NpcId:" + getId());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final double buyTaxRate = (applyTax) ? getMpc().getTotalTaxRate(TaxType.BUY) : 0;
		final double sellTaxRate = (applyTax) ? getMpc().getTotalTaxRate(TaxType.SELL) : 0;
		
		player.setInventoryBlockingStatus(true);
		
		player.sendPacket(new BuyList(buyList, player.getAdena(), buyTaxRate));
		player.sendPacket(new ExBuySellList(player, false, sellTaxRate));
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
