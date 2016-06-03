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
package org.l2junity.loginserver.network.client.recv;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.l2junity.loginserver.network.client.ClientHandler;
import org.l2junity.loginserver.network.client.send.LoginFail2;
import org.l2junity.network.IIncomingPacket;
import org.l2junity.network.PacketReader;

/**
 * @author NosBit
 */
public class RequestSCCheck implements IIncomingPacket<ClientHandler>
{
	private int _unk1;
	private byte[] _raw;
	
	/*
	 * (non-Javadoc)
	 * @see org.l2junity.network.IIncomingPacket#read(org.l2junity.network.PacketReader)
	 */
	@Override
	public boolean read(ClientHandler client, PacketReader packet)
	{
		_unk1 = packet.readD();
		_raw = packet.readB(128);
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2junity.network.IIncomingPacket#run(java.lang.Object)
	 */
	@Override
	public void run(ClientHandler client)
	{
		byte[] decrypted = new byte[128];
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getScrambledRSAKeyPair().getPrivateKey());
			rsaCipher.doFinal(_raw, 0x00, 0x80, decrypted, 0);
		}
		catch (GeneralSecurityException e)
		{
		}
		
		System.out.println(_unk1);
		int num = (decrypted[0x7C] & 0xFF) | ((decrypted[0x7D] & 0xFF) << 8) | ((decrypted[0x7E] & 0xFF) << 16) | ((decrypted[0x7F] & 0xFF) << 24);
		System.out.println(num);
		client.close(LoginFail2.ACCESS_FAILED);
	}
}
