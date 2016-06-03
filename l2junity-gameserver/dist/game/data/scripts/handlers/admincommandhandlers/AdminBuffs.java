/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.skills.AbnormalType;
import org.l2junity.gameserver.model.skills.BuffInfo;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.SkillCoolTime;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.GMAudit;

public class AdminBuffs implements IAdminCommandHandler
{
	private final static int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buff",
		"admin_getbuffs",
		"admin_getbuffs_ps",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel",
		"admin_removereuse",
		"admin_switch_gm_buffs"
	};
	// Misc
	private static final String FONT_RED1 = "<font color=\"FF0000\">";
	private static final String FONT_RED2 = "</font>";
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("admin_buff"))
		{
			if ((activeChar.getTarget() == null) || !activeChar.getTarget().isCreature())
			{
				activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
			
			final StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Skill Id and level are not specified.");
				activeChar.sendMessage("Usage: //buff <skillId> <skillLevel>");
				return false;
			}
			
			try
			{
				final int skillId = Integer.parseInt(st.nextToken());
				final int skillLevel = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : SkillData.getInstance().getMaxLevel(skillId);
				final Creature target = (Creature) activeChar.getTarget();
				final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
				if (skill == null)
				{
					activeChar.sendMessage("Skill with id: " + skillId + ", lvl: " + skillLevel + " not found.");
					return false;
				}
				
				activeChar.sendMessage("Admin buffing " + skill.getName() + " (" + skillId + "," + skillLevel + ")");
				skill.applyEffects(activeChar, target);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed buffing: " + e.getMessage());
				activeChar.sendMessage("Usage: //buff <skillId> <skillLevel>");
				return false;
			}
		}
		else if (command.startsWith("admin_getbuffs"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			if (st.hasMoreTokens())
			{
				final String playername = st.nextToken();
				PlayerInstance player = World.getInstance().getPlayer(playername);
				if (player != null)
				{
					int page = 1;
					if (st.hasMoreTokens())
					{
						page = Integer.parseInt(st.nextToken());
					}
					showBuffs(activeChar, player, page, command.endsWith("_ps"));
					return true;
				}
				activeChar.sendMessage("The player " + playername + " is not online.");
				return false;
			}
			else if ((activeChar.getTarget() != null) && activeChar.getTarget().isCreature())
			{
				showBuffs(activeChar, (Creature) activeChar.getTarget(), 1, command.endsWith("_ps"));
				return true;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}
		else if (command.startsWith("admin_stopbuff"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				
				removeBuff(activeChar, objectId, skillId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed removing effect: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>");
				return false;
			}
		}
		else if (command.startsWith("admin_stopallbuffs"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				removeAllBuffs(activeChar, objectId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed removing all effects: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopallbuffs <objectId>");
				return false;
			}
		}
		else if (command.startsWith("admin_areacancel"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String val = st.nextToken();
			try
			{
				int radius = Integer.parseInt(val);
				
				World.getInstance().forEachVisibleObjectInRange(activeChar, PlayerInstance.class, radius, Creature::stopAllEffects);
				
				activeChar.sendMessage("All effects canceled within radius " + radius);
				return true;
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //areacancel <radius>");
				return false;
			}
		}
		else if (command.startsWith("admin_removereuse"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			
			PlayerInstance player = null;
			if (st.hasMoreTokens())
			{
				String playername = st.nextToken();
				
				try
				{
					player = World.getInstance().getPlayer(playername);
				}
				catch (Exception e)
				{
				}
				
				if (player == null)
				{
					activeChar.sendMessage("The player " + playername + " is not online.");
					return false;
				}
			}
			else if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				player = activeChar.getTarget().getActingPlayer();
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
			
			try
			{
				player.resetTimeStamps();
				player.resetDisabledSkills();
				player.sendPacket(new SkillCoolTime(player));
				activeChar.sendMessage("Skill reuse was removed from " + player.getName() + ".");
				return true;
			}
			catch (NullPointerException e)
			{
				return false;
			}
		}
		else if (command.startsWith("admin_switch_gm_buffs"))
		{
			if (Config.GM_GIVE_SPECIAL_SKILLS != Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				final boolean toAuraSkills = activeChar.getKnownSkill(7041) != null;
				switchSkills(activeChar, toAuraSkills);
				activeChar.sendSkillList();
				activeChar.sendMessage("You have succefully changed to target " + (toAuraSkills ? "aura" : "one") + " special skills.");
				return true;
			}
			activeChar.sendMessage("There is nothing to switch.");
			return false;
		}
		return true;
	}
	
	/**
	 * @param gmchar the player to switch the Game Master skills.
	 * @param toAuraSkills if {@code true} it will remove "GM Aura" skills and add "GM regular" skills, vice versa if {@code false}.
	 */
	public static void switchSkills(PlayerInstance gmchar, boolean toAuraSkills)
	{
		final Collection<Skill> skills = toAuraSkills ? SkillTreesData.getInstance().getGMSkillTree().values() : SkillTreesData.getInstance().getGMAuraSkillTree().values();
		for (Skill skill : skills)
		{
			gmchar.removeSkill(skill, false); // Don't Save GM skills to database
		}
		SkillTreesData.getInstance().addSkills(gmchar, toAuraSkills);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public static void showBuffs(PlayerInstance activeChar, Creature target, int page, boolean passive)
	{
		final List<BuffInfo> effects = new ArrayList<>();
		if (!passive)
		{
			effects.addAll(target.getEffectList().getEffects());
		}
		else
		{
			effects.addAll(target.getEffectList().getPassives());
		}
		
		if ((page > ((effects.size() / PAGE_LIMIT) + 1)) || (page < 1))
		{
			return;
		}
		
		int max = effects.size() / PAGE_LIMIT;
		if (effects.size() > (PAGE_LIMIT * max))
		{
			max++;
		}
		
		final StringBuilder html = new StringBuilder(500 + (effects.size() * 200));
		html.append("<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center><font color=\"LEVEL\">Effects of ");
		html.append(target.getName());
		html.append("</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"100%\"><tr><td width=200>Skill</td><td width=30>Rem. Time</td><td width=70>Action</td></tr>");
		
		int start = ((page - 1) * PAGE_LIMIT);
		int end = Math.min(((page - 1) * PAGE_LIMIT) + PAGE_LIMIT, effects.size());
		int count = 0;
		for (BuffInfo info : effects)
		{
			if ((count >= start) && (count < end))
			{
				final Skill skill = info.getSkill();
				for (AbstractEffect effect : info.getEffects())
				{
					html.append("<tr><td>");
					html.append(!info.isInUse() ? FONT_RED1 : "");
					html.append(skill.getName());
					html.append(" Lv ");
					html.append(skill.getLevel());
					html.append(" (");
					html.append(effect.getClass().getSimpleName());
					html.append(")");
					html.append(!info.isInUse() ? FONT_RED2 : "");
					html.append("</td><td>");
					html.append(skill.isToggle() ? "T (" + info.getTickCount(effect) + ")" : skill.isPassive() ? "P" : info.getTime() + "s");
					html.append("</td><td><button value=\"X\" action=\"bypass -h admin_stopbuff ");
					html.append(target.getObjectId());
					html.append(" ");
					html.append(skill.getId());
					html.append("\" width=30 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				}
			}
			count++;
		}
		
		html.append("</table><table width=300 bgcolor=444444><tr>");
		for (int x = 0; x < max; x++)
		{
			int pagenr = x + 1;
			if (page == pagenr)
			{
				html.append("<td>Page ");
				html.append(pagenr);
				html.append("</td>");
			}
			else
			{
				html.append("<td><a action=\"bypass -h admin_getbuffs" + (passive ? "_ps " : " "));
				html.append(target.getName());
				html.append(" ");
				html.append(x + 1);
				html.append("\"> Page ");
				html.append(pagenr);
				html.append(" </a></td>");
			}
		}
		
		html.append("</tr></table>");
		
		// Buttons
		html.append("<br><center><button value=\"Refresh\" action=\"bypass -h admin_getbuffs");
		html.append(passive ? "_ps " : " ");
		html.append(target.getName());
		html.append("\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		html.append("<button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ");
		html.append(target.getObjectId());
		html.append("\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>");
		// Legend
		if (!passive)
		{
			html.append(FONT_RED1);
			html.append("Inactive buffs: ");
			html.append(target.getEffectList().getHiddenBuffsCount());
			html.append(FONT_RED2);
			html.append("<br>");
		}
		html.append("Total");
		html.append(passive ? " passive" : "");
		html.append(" buff count: ");
		html.append(effects.size());
		if ((target.getEffectList().getBlockedAbnormalTypes() != null) && !target.getEffectList().getBlockedAbnormalTypes().isEmpty())
		{
			html.append("<br>Blocked buff slots: ");
			String slots = "";
			for (AbnormalType slot : target.getEffectList().getBlockedAbnormalTypes())
			{
				slots += slot.toString() + ", ";
			}
			
			if (!slots.isEmpty() && (slots.length() > 3))
			{
				html.append(slots.substring(0, slots.length() - 2));
			}
		}
		html.append("</html>");
		// Send the packet
		activeChar.sendPacket(new NpcHtmlMessage(0, 1, html.toString()));
		
		if (Config.GMAUDIT)
		{
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "getbuffs", target.getName() + " (" + Integer.toString(target.getObjectId()) + ")", "");
		}
	}
	
	private static void removeBuff(PlayerInstance activeChar, int objId, int skillId)
	{
		Creature target = null;
		try
		{
			target = (Creature) World.getInstance().findObject(objId);
		}
		catch (Exception e)
		{
		}
		
		if ((target != null) && (skillId > 0))
		{
			if (target.isAffectedBySkill(skillId))
			{
				target.stopSkillEffects(true, skillId);
				activeChar.sendMessage("Removed skill ID: " + skillId + " effects from " + target.getName() + " (" + objId + ").");
			}
			
			showBuffs(activeChar, target, 1, false);
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopbuff", target.getName() + " (" + objId + ")", Integer.toString(skillId));
			}
		}
	}
	
	private static void removeAllBuffs(PlayerInstance activeChar, int objId)
	{
		Creature target = null;
		try
		{
			target = (Creature) World.getInstance().findObject(objId);
		}
		catch (Exception e)
		{
		}
		
		if (target != null)
		{
			target.stopAllEffects();
			activeChar.sendMessage("Removed all effects from " + target.getName() + " (" + objId + ")");
			showBuffs(activeChar, target, 1, false);
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopallbuffs", target.getName() + " (" + objId + ")", "");
			}
		}
	}
}
