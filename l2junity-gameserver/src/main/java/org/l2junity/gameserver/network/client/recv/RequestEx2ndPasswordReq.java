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

import org.l2junity.gameserver.data.xml.impl.SecondaryAuthData;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.Ex2ndPasswordAck;
import org.l2junity.gameserver.security.SecondaryPasswordAuth;
import org.l2junity.network.PacketReader;

/**
 * (ch)cS{S} c: change pass? S: current password S: new password
 * @author mrTJO
 */
public class RequestEx2ndPasswordReq implements IClientIncomingPacket
{
	private int _changePass;
	private String _password, _newPassword;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_changePass = packet.readC();
		_password = packet.readS();
		if (_changePass == 2)
		{
			_newPassword = packet.readS();
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if (!SecondaryAuthData.getInstance().isEnabled())
		{
			return;
		}
		
		SecondaryPasswordAuth secondAuth = client.getSecondaryAuth();
		boolean success = false;
		
		if ((_changePass == 0) && !secondAuth.passwordExist())
		{
			success = secondAuth.savePassword(_password);
		}
		else if ((_changePass == 2) && secondAuth.passwordExist())
		{
			success = secondAuth.changePassword(_password, _newPassword);
		}
		
		if (success)
		{
			client.sendPacket(new Ex2ndPasswordAck(_changePass, Ex2ndPasswordAck.SUCCESS));
		}
	}
}
