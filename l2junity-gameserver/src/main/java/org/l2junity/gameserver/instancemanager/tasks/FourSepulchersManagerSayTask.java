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
package org.l2junity.gameserver.instancemanager.tasks;

import java.util.Calendar;

import org.l2junity.Config;
import org.l2junity.gameserver.ThreadPoolManager;
import org.l2junity.gameserver.instancemanager.FourSepulchersManager;

/**
 * Four Sepulchers manager say task.
 * @author xban1x
 */
public final class FourSepulchersManagerSayTask implements Runnable
{
	@Override
	public void run()
	{
		if (FourSepulchersManager.getInstance().isAttackTime())
		{
			final Calendar tmp = Calendar.getInstance();
			tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - FourSepulchersManager.getInstance().getWarmUpTimeEnd());
			if ((tmp.get(Calendar.MINUTE) + 5) < Config.FS_TIME_ATTACK)
			{
				FourSepulchersManager.getInstance().managerSay((byte) tmp.get(Calendar.MINUTE)); // byte
				// because
				// minute
				// cannot be
				// more than
				// 59
				ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), 5 * 60000);
			}
			// attack time ending chat
			else if ((tmp.get(Calendar.MINUTE) + 5) >= Config.FS_TIME_ATTACK)
			{
				FourSepulchersManager.getInstance().managerSay((byte) 90); // sending a unique id :D
			}
		}
		else if (FourSepulchersManager.getInstance().isEntryTime())
		{
			FourSepulchersManager.getInstance().managerSay((byte) 0);
		}
	}
}