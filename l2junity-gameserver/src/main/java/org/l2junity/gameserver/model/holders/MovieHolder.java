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
package org.l2junity.gameserver.model.holders;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2junity.gameserver.enums.Movie;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author St3eT
 */
public final class MovieHolder
{
	private final Movie _movie;
	private final List<PlayerInstance> _players;
	private final List<PlayerInstance> _votedPlayers = new CopyOnWriteArrayList<>();
	
	public MovieHolder(List<PlayerInstance> players, Movie movie)
	{
		_players = players;
		_movie = movie;
		
		getPlayers().forEach(p -> p.playMovie(this));
	}
	
	public Movie getMovie()
	{
		return _movie;
	}
	
	public void playerEscapeVote(PlayerInstance player)
	{
		if (getVotedPlayers().contains(player) || !getPlayers().contains(player) || !getMovie().isEscapable())
		{
			return;
		}
		
		getVotedPlayers().add(player);
		
		if (((getVotedPlayers().size() * 100) / getPlayers().size()) >= 50)
		{
			getPlayers().forEach(PlayerInstance::stopMovie);
		}
	}
	
	public List<PlayerInstance> getPlayers()
	{
		return _players;
	}
	
	public List<PlayerInstance> getVotedPlayers()
	{
		return _votedPlayers;
	}
}