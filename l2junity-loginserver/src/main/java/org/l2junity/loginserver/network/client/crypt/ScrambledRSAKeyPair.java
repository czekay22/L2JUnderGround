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
package org.l2junity.loginserver.network.client.crypt;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * A wrapper of {@code RSAKeyPair} that provides scrambled modulus required by login server protocol.
 * @author NosBit
 */
public class ScrambledRSAKeyPair
{
	private final KeyPair _pair;
	private final byte[] _scrambledModulus;
	
	public ScrambledRSAKeyPair(KeyPair pair)
	{
		_pair = pair;
		
		byte[] scrambledModulus = ((RSAPublicKey) _pair.getPublic()).getModulus().toByteArray();
		if ((scrambledModulus.length == 0x81) && (scrambledModulus[0] == 0))
		{
			scrambledModulus = Arrays.copyOfRange(scrambledModulus, 1, 0x81);
		}
		
		// step 1 : 0x4d-0x50 <-> 0x00-0x04
		for (int i = 0; i < 4; i++)
		{
			byte temp = scrambledModulus[0x00 + i];
			scrambledModulus[0x00 + i] = scrambledModulus[0x4d + i];
			scrambledModulus[0x4d + i] = temp;
		}
		
		// step 2 : xor first 0x40 bytes with last 0x40 bytes
		for (int i = 0; i < 0x40; i++)
		{
			scrambledModulus[i] = (byte) (scrambledModulus[i] ^ scrambledModulus[0x40 + i]);
		}
		
		// step 3 : xor bytes 0x0d-0x10 with bytes 0x34-0x38
		for (int i = 0; i < 4; i++)
		{
			scrambledModulus[0x0d + i] = (byte) (scrambledModulus[0x0d + i] ^ scrambledModulus[0x34 + i]);
		}
		
		// step 4 : xor last 0x40 bytes with first 0x40 bytes
		for (int i = 0; i < 0x40; i++)
		{
			scrambledModulus[0x40 + i] = (byte) (scrambledModulus[0x40 + i] ^ scrambledModulus[i]);
		}
		
		_scrambledModulus = scrambledModulus;
	}
	
	/**
	 * Gets the private key.
	 * @return the private key
	 */
	public PrivateKey getPrivateKey()
	{
		return _pair.getPrivate();
	}
	
	/**
	 * Gets the scrambled modulus.
	 * @return the scrambled modulus
	 */
	public byte[] getScrambledModulus()
	{
		return _scrambledModulus;
	}
}
