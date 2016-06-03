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
package org.l2junity.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.l2junity.Config;
import org.l2junity.loginserver.GameServerThread;
import org.l2junity.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import org.l2junity.util.crypt.NewCrypt;
import org.l2junity.util.network.BaseRecievePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends BaseRecievePacket
{
	protected static final Logger _log = LoggerFactory.getLogger(BlowFishKey.class);
	
	/**
	 * @param decrypt
	 * @param server
	 */
	public BlowFishKey(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		int size = readD();
		byte[] tempKey = readB(size);
		try
		{
			byte[] tempDecryptKey;
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, server.getPrivateKey());
			tempDecryptKey = rsaCipher.doFinal(tempKey);
			// there are nulls before the key we must remove them
			int i = 0;
			int len = tempDecryptKey.length;
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
				{
					break;
				}
			}
			byte[] key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, key, 0, len - i);
			
			server.SetBlowFish(new NewCrypt(key));
			if (Config.DEBUG)
			{
				_log.info("New BlowFish key received, Blowfih Engine initialized:");
			}
			server.setLoginConnectionState(GameServerState.BF_CONNECTED);
		}
		catch (GeneralSecurityException e)
		{
			_log.error("Error While decrypting blowfish key (RSA): " + e.getMessage(), e);
		}
	}
}
