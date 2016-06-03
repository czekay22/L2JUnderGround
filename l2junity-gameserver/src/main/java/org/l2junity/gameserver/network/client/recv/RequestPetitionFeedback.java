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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.l2junity.DatabaseFactory;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.network.PacketReader;

/**
 * @author Plim
 */
public class RequestPetitionFeedback implements IClientIncomingPacket
{
	private static final String INSERT_FEEDBACK = "INSERT INTO petition_feedback VALUES (?,?,?,?,?)";
	
	// cdds
	// private int _unknown;
	private int _rate; // 4=VeryGood, 3=Good, 2=Fair, 1=Poor, 0=VeryPoor
	private String _message;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		// _unknown =
		packet.readD(); // unknown
		_rate = packet.readD();
		_message = packet.readS();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		PlayerInstance player = client.getActiveChar();
		
		if ((player == null) || (player.getLastPetitionGmName() == null))
		{
			return;
		}
		
		if ((_rate > 4) || (_rate < 0))
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_FEEDBACK))
		{
			statement.setString(1, player.getName());
			statement.setString(2, player.getLastPetitionGmName());
			statement.setInt(3, _rate);
			statement.setString(4, _message);
			statement.setLong(5, System.currentTimeMillis());
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.error("Error while saving petition feedback");
		}
	}
	
}
