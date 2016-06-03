/*
 * Copyright (C) 2004-2016 L2J Unity
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
package org.l2junity.loginserver.network.clientpackets;

import org.l2junity.Config;
import org.l2junity.loginserver.network.serverpackets.PIAgreementCheck;

/**
 * @author UnAfraid
 */
public class RequestPIAgreementCheck extends L2LoginClientPacket
{
	private int _accountId;
	
	@Override
	protected boolean readImpl()
	{
		_accountId = readD();
		byte[] padding0 = new byte[3];
		byte[] checksum = new byte[4];
		byte[] padding1 = new byte[12];
		readB(padding0);
		readB(checksum);
		readB(padding1);
		return true;
	}
	
	@Override
	public void run()
	{
		getClient().sendPacket(new PIAgreementCheck(_accountId, Config.SHOW_PI_AGREEMENT ? 0x01 : 0x00));
	}
}
