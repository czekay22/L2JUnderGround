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

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.l2junity.commons.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the keys and key pairs required for network communication.
 * @author NosBit
 */
public class KeyManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyManager.class);
	
	private final KeyGenerator _blowfishKeyGenerator;
	private final ScrambledRSAKeyPair[] _scrambledRSAKeyPairs = new ScrambledRSAKeyPair[50];
	
	protected KeyManager() throws GeneralSecurityException
	{
		_blowfishKeyGenerator = KeyGenerator.getInstance("Blowfish");
		
		final KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		rsaKeyPairGenerator.initialize(spec);
		
		for (int i = 0; i < _scrambledRSAKeyPairs.length; i++)
		{
			_scrambledRSAKeyPairs[i] = new ScrambledRSAKeyPair(rsaKeyPairGenerator.generateKeyPair());
		}
		
		LOGGER.info("Cached {} RSA key pairs.", _scrambledRSAKeyPairs.length);
	}
	
	/**
	 * Generates a Blowfish key.
	 * @return the blowfish {@code SecretKey}
	 */
	public SecretKey generateBlowfishKey()
	{
		return _blowfishKeyGenerator.generateKey();
	}
	
	/**
	 * Gets a random pre-cached {@code ScrambledRSAKeyPair}.
	 * @return the {@code ScrambledRSAKeyPair}
	 */
	public ScrambledRSAKeyPair getRandomScrambledRSAKeyPair()
	{
		return _scrambledRSAKeyPairs[Rnd.nextInt(_scrambledRSAKeyPairs.length)];
	}
	
	/**
	 * Gets the single instance of {@code KeyGen}.
	 * @return the instance
	 */
	public static KeyManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final KeyManager _instance;
		
		static
		{
			KeyManager instance = null;
			try
			{
				instance = new KeyManager();
			}
			catch (GeneralSecurityException e)
			{
				e.printStackTrace();
			}
			finally
			{
				_instance = instance;
			}
		}
	}
}
