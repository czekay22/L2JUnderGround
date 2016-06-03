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

import org.l2junity.gameserver.enums.HtmlActionScope;
import org.l2junity.gameserver.network.client.OutgoingPackets;
import org.l2junity.network.PacketWriter;

/**
 * TutorialShowHtml server packet implementation.
 * @author HorridoJoho
 */
public final class TutorialShowHtml extends AbstractHtmlPacket
{
	// TODO: Enum
	public static final int NORMAL_WINDOW = 1;
	public static final int LARGE_WINDOW = 2;
	
	private final int _type;
	
	public TutorialShowHtml(String html)
	{
		super(html);
		_type = NORMAL_WINDOW;
	}
	
	/**
	 * This constructor is just here to be able to show a tutorial html<br>
	 * window bound to an npc.
	 * @param npcObjId
	 * @param html
	 */
	public TutorialShowHtml(int npcObjId, String html)
	{
		super(npcObjId, html);
		_type = NORMAL_WINDOW;
	}
	
	public TutorialShowHtml(int npcObjId, String html, int type)
	{
		super(npcObjId, html);
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.TUTORIAL_SHOW_HTML.writeId(packet);
		
		packet.writeD(_type);
		packet.writeS(getHtml());
		return true;
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}
