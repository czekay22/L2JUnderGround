/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.gameserver.model.html.styles;

import org.l2junity.gameserver.model.html.IHtmlStyle;

/**
 * @author UnAfraid
 */
public class DefaultStyle implements IHtmlStyle
{
	private static final String DEFAULT_PAGE_LINK_FORMAT = "<td><a action=\"%s\">%s</a></td>";
	private static final String DEFAULT_PAGE_TEXT_FORMAT = "<td>%s</td>";
	private static final String DEFAULT_PAGER_SEPARATOR = "<td align=center> | </td>";
	
	public static final DefaultStyle INSTANCE = new DefaultStyle();
	
	@Override
	public String applyBypass(String bypass, String name, boolean isEnabled)
	{
		if (isEnabled)
		{
			return String.format(DEFAULT_PAGE_TEXT_FORMAT, name);
		}
		return String.format(DEFAULT_PAGE_LINK_FORMAT, bypass, name);
	}
	
	@Override
	public String applySeparator()
	{
		return DEFAULT_PAGER_SEPARATOR;
	}
}
