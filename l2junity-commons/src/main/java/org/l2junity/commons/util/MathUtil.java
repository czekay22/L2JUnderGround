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
package org.l2junity.commons.util;

/**
 * @author UnAfraid
 */
public class MathUtil
{
	public static byte add(byte oldValue, byte value)
	{
		return (byte) (oldValue + value);
	}
	
	public static short add(short oldValue, short value)
	{
		return (short) (oldValue + value);
	}
	
	public static int add(int oldValue, int value)
	{
		return oldValue + value;
	}
	
	public static double add(double oldValue, double value)
	{
		return oldValue + value;
	}
	
	public static byte mul(byte oldValue, byte value)
	{
		return (byte) (oldValue * value);
	}
	
	public static short mul(short oldValue, short value)
	{
		return (short) (oldValue * value);
	}
	
	public static int mul(int oldValue, int value)
	{
		return oldValue * value;
	}
	
	public static double mul(double oldValue, double value)
	{
		return oldValue * value;
	}
	
	public static byte div(byte oldValue, byte value)
	{
		return (byte) (oldValue / value);
	}
	
	public static short div(short oldValue, short value)
	{
		return (short) (oldValue / value);
	}
	
	public static int div(int oldValue, int value)
	{
		return oldValue / value;
	}
	
	public static double div(double oldValue, double value)
	{
		return oldValue / value;
	}
}
