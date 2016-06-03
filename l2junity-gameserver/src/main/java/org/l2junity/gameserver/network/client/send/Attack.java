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
package org.l2junity.gameserver.network.client.send;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.l2junity.gameserver.model.Hit;
import org.l2junity.gameserver.model.Location;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

public class Attack implements IClientOutgoingPacket
{
	private final int _attackerObjId;
	private final boolean _soulshot;
	private final int _ssGrade;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	
	/**
	 * @param attacker
	 * @param target
	 * @param useShots
	 * @param ssGrade
	 */
	public Attack(Creature attacker, Creature target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_soulshot = useShots;
		_ssGrade = Math.min(ssGrade, 6);
		_attackerLoc = new Location(attacker);
		_targetLoc = new Location(target);
	}
	
	/**
	 * Adds hit to the attack (Attacks such as dual dagger/sword/fist has two hits)
	 * @param target
	 * @param damage
	 * @param miss
	 * @param crit
	 * @param shld
	 */
	public void addHit(Creature target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits.add(new Hit(target, damage, miss, crit, shld, _soulshot, _ssGrade));
	}
	
	/**
	 * @return {@code true} if current attack contains at least 1 hit.
	 */
	public boolean hasHits()
	{
		return !_hits.isEmpty();
	}
	
	/**
	 * @return {@code true} if attack has soul shot charged.
	 */
	public boolean hasSoulshot()
	{
		return _soulshot;
	}
	
	/**
	 * Writes current hit
	 * @param packet
	 * @param hit
	 */
	private void writeHit(PacketWriter packet, Hit hit)
	{
		packet.writeD(hit.getTargetId());
		packet.writeD(hit.getDamage());
		packet.writeD(hit.getFlags());
		packet.writeD(hit.getGrade()); // GOD
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		final Iterator<Hit> it = _hits.iterator();
		final Hit firstHit = it.next();
		OutgoingPackets.ATTACK.writeId(packet);
		
		packet.writeD(_attackerObjId);
		packet.writeD(firstHit.getTargetId());
		packet.writeD(0x00); // Ertheia Unknown
		packet.writeD(firstHit.getDamage());
		packet.writeD(firstHit.getFlags());
		packet.writeD(firstHit.getGrade()); // GOD
		packet.writeD(_attackerLoc.getX());
		packet.writeD(_attackerLoc.getY());
		packet.writeD(_attackerLoc.getZ());
		
		packet.writeH(_hits.size() - 1);
		while (it.hasNext())
		{
			writeHit(packet, it.next());
		}
		
		packet.writeD(_targetLoc.getX());
		packet.writeD(_targetLoc.getY());
		packet.writeD(_targetLoc.getZ());
		return true;
	}
}