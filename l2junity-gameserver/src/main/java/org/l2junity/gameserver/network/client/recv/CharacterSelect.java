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

import org.l2junity.Config;
import org.l2junity.gameserver.data.sql.impl.CharNameTable;
import org.l2junity.gameserver.data.xml.impl.SecondaryAuthData;
import org.l2junity.gameserver.instancemanager.AntiFeedManager;
import org.l2junity.gameserver.instancemanager.PunishmentManager;
import org.l2junity.gameserver.model.CharSelectInfoPackage;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.events.Containers;
import org.l2junity.gameserver.model.events.EventDispatcher;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerSelect;
import org.l2junity.gameserver.model.events.returns.TerminateReturn;
import org.l2junity.gameserver.model.punishment.PunishmentAffect;
import org.l2junity.gameserver.model.punishment.PunishmentType;
import org.l2junity.gameserver.network.client.ConnectionState;
import org.l2junity.gameserver.network.client.L2GameClient;
import org.l2junity.gameserver.network.client.send.CharSelected;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.ServerClose;
import org.l2junity.network.PacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelect implements IClientIncomingPacket
{
	protected static final Logger _logAccounting = LoggerFactory.getLogger("accounting");
	
	// cd
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_charSlot = packet.readD();
		_unk1 = packet.readH();
		_unk2 = packet.readD();
		_unk3 = packet.readD();
		_unk4 = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		if (!client.getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		if (SecondaryAuthData.getInstance().isEnabled() && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		// We should always be able to acquire the lock
		// But if we can't lock then nothing should be done (i.e. repeated packet)
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if (client.getActiveChar() == null)
				{
					final CharSelectInfoPackage info = client.getCharSelection(_charSlot);
					if (info == null)
					{
						return;
					}
					
					// Banned?
					if (PunishmentManager.getInstance().hasPunishment(info.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getConnectionAddress().getHostAddress(), PunishmentAffect.IP, PunishmentType.BAN))
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					
					// Selected character is banned (compatibility with previous versions).
					if (info.getAccessLevel() < 0)
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					
					if ((Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddClient(AntiFeedManager.GAME_ID, client, Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP))
					{
						final NpcHtmlMessage msg = new NpcHtmlMessage();
						msg.setFile(info.getHtmlPrefix(), "data/html/mods/IPRestriction.htm");
						msg.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(client, Config.L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP)));
						client.sendPacket(msg);
						return;
					}
					
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if (Config.DEBUG)
					{
						_log.debug("selected slot:" + _charSlot);
					}
					
					// load up character from disk
					final PlayerInstance cha = client.loadCharFromDisk(_charSlot);
					if (cha == null)
					{
						return; // handled in L2GameClient
					}
					
					CharNameTable.getInstance().addName(cha);
					
					cha.setClient(client);
					client.setActiveChar(cha);
					cha.setOnlineStatus(true, true);
					
					final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerSelect(cha, cha.getObjectId(), cha.getName(), client), Containers.Players(), TerminateReturn.class);
					if ((terminate != null) && terminate.terminate())
					{
						cha.deleteMe();
						return;
					}
					
					client.setConnectionState(ConnectionState.IN_GAME);
					client.sendPacket(new CharSelected(cha, client.getSessionId().playOkID1));
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
			
			_logAccounting.info("Logged in, {}", client);
		}
	}
}
