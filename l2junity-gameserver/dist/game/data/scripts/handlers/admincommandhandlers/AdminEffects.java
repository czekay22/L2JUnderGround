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

import java.util.Arrays;
import java.util.StringTokenizer;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.SkillData;
import org.l2junity.gameserver.enums.Movie;
import org.l2junity.gameserver.enums.Team;
import org.l2junity.gameserver.handler.IAdminCommandHandler;
import org.l2junity.gameserver.model.World;
import org.l2junity.gameserver.model.WorldObject;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.Npc;
import org.l2junity.gameserver.model.actor.instance.L2ChestInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.holders.MovieHolder;
import org.l2junity.gameserver.model.html.PageBuilder;
import org.l2junity.gameserver.model.html.PageResult;
import org.l2junity.gameserver.model.html.styles.ButtonsStyle;
import org.l2junity.gameserver.model.skills.AbnormalVisualEffect;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.Earthquake;
import org.l2junity.gameserver.network.client.send.ExRedSky;
import org.l2junity.gameserver.network.client.send.ExUserInfoAbnormalVisualEffect;
import org.l2junity.gameserver.network.client.send.IClientOutgoingPacket;
import org.l2junity.gameserver.network.client.send.MagicSkillUse;
import org.l2junity.gameserver.network.client.send.NpcHtmlMessage;
import org.l2junity.gameserver.network.client.send.OnEventTrigger;
import org.l2junity.gameserver.network.client.send.PlaySound;
import org.l2junity.gameserver.network.client.send.SocialAction;
import org.l2junity.gameserver.network.client.send.SunRise;
import org.l2junity.gameserver.network.client.send.SunSet;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;
import org.l2junity.gameserver.util.Broadcast;
import org.l2junity.gameserver.util.Util;

/**
 * This class handles following admin commands:
 * <li>invis/invisible/vis/visible = makes yourself invisible or visible
 * <li>earthquake = causes an earthquake of a given intensity and duration around you
 * <li>bighead/shrinkhead = changes head size
 * <li>gmspeed = temporary Super Haste effect.
 * <li>para/unpara = paralyze/remove paralysis from target
 * <li>para_all/unpara_all = same as para/unpara, affects the whole world.
 * <li>polyself/unpolyself = makes you look as a specified mob.
 * <li>changename = temporary change name
 * <li>clearteams/setteam_close/setteam = team related commands
 * <li>social = forces an L2Character instance to broadcast social action packets.
 * <li>effect = forces an L2Character instance to broadcast MSU packets.
 * <li>abnormal = force changes over an L2Character instance's abnormal state.
 * <li>play_sound/play_sounds = Music broadcasting related commands
 * <li>atmosphere = sky change related commands.
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invis",
		"admin_invisible",
		"admin_setinvis",
		"admin_vis",
		"admin_visible",
		"admin_invis_menu",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself",
		"admin_unpolyself",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_effect_menu",
		"admin_ave_abnormal",
		"admin_social_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu",
		"admin_set_displayeffect",
		"admin_set_displayeffect_menu",
		"admin_event_trigger",
		"admin_settargetable",
		"admin_playmovie",
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.equals("admin_invis_menu"))
		{
			if (!activeChar.isInvisible())
			{
				activeChar.setInvisible(true);
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));
				activeChar.decayMe();
				activeChar.spawnMe();
				activeChar.sendMessage("You are now invisible.");
			}
			else
			{
				activeChar.setInvisible(false);
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));
				activeChar.sendMessage("You are now visible.");
			}
			
			command = "";
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_invis"))
		{
			activeChar.setInvisible(true);
			activeChar.broadcastUserInfo();
			activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));
			activeChar.decayMe();
			activeChar.spawnMe();
			activeChar.sendMessage("You are now invisible.");
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.setInvisible(false);
			activeChar.broadcastUserInfo();
			activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));
			activeChar.sendMessage("You are now visible.");
		}
		else if (command.startsWith("admin_setinvis"))
		{
			if ((activeChar.getTarget() == null) || !activeChar.getTarget().isCreature())
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			final Creature target = (Creature) activeChar.getTarget();
			target.setInvisible(!target.isInvisible());
			activeChar.sendMessage("You've made " + target.getName() + " " + (target.isInvisible() ? "invisible" : "visible") + ".");
			
			if (target.isPlayer())
			{
				((PlayerInstance) target).broadcastUserInfo();
			}
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				int duration = Integer.parseInt(st.nextToken());
				adminAtmosphere(type, state, duration, activeChar);
			}
			catch (Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
			}
		}
		else if (command.equals("admin_play_sounds"))
		{
			AdminHtml.showAdminHtml(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHtml.showAdminHtml(activeChar, "songs/songs" + command.substring(18) + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sound <soundname>");
			}
		}
		else if (command.equals("admin_para_all"))
		{
			World.getInstance().forEachVisibleObject(activeChar, PlayerInstance.class, player ->
			{
				if (!player.isGM())
				{
					player.startAbnormalVisualEffect(AbnormalVisualEffect.PARALYZE);
					player.setBlockActions(true);
					player.startParalyze();
					player.broadcastInfo();
				}
			});
		}
		else if (command.equals("admin_unpara_all"))
		{
			World.getInstance().forEachVisibleObject(activeChar, PlayerInstance.class, player ->
			{
				player.stopAbnormalVisualEffect(AbnormalVisualEffect.PARALYZE);
				player.setBlockActions(false);
				player.broadcastInfo();
				
			});
		}
		else if (command.startsWith("admin_para")) // || command.startsWith("admin_para_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature)
				{
					player = (Creature) target;
					if (type.equals("1"))
					{
						player.startAbnormalVisualEffect(AbnormalVisualEffect.PARALYZE);
					}
					else
					{
						player.startAbnormalVisualEffect(AbnormalVisualEffect.FLESH_STONE);
					}
					player.setBlockActions(true);
					player.startParalyze();
					player.broadcastInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpara")) // || command.startsWith("admin_unpara_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature)
				{
					player = (Creature) target;
					if (type.equals("1"))
					{
						player.stopAbnormalVisualEffect(AbnormalVisualEffect.PARALYZE);
					}
					else
					{
						player.stopAbnormalVisualEffect(AbnormalVisualEffect.FLESH_STONE);
					}
					player.setBlockActions(false);
					player.broadcastInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature)
				{
					player = (Creature) target;
					player.startAbnormalVisualEffect(AbnormalVisualEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature)
				{
					player = (Creature) target;
					player.stopAbnormalVisualEffect(AbnormalVisualEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				final boolean sendMessage = activeChar.isAffectedBySkill(7029);
				activeChar.stopSkillEffects((val == 0) && sendMessage, 7029);
				if ((val >= 1) && (val <= 4))
				{
					int time = 0;
					if (st.hasMoreTokens())
					{
						time = Integer.parseInt(st.nextToken());
					}
					
					final Skill gmSpeedSkill = SkillData.getInstance().getSkill(7029, val);
					gmSpeedSkill.applyEffects(activeChar, activeChar, true, time);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //gmspeed <Effect level (0-4)> <Time in seconds>");
			}
			if (command.contains("_menu"))
			{
				command = "";
				AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
			}
		}
		else if (command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();
				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getLocation());
				activeChar.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polyself <npcId>");
			}
		}
		else if (command.startsWith("admin_unpolyself"))
		{
			activeChar.getPoly().setPolyInfo(null, "1");
			activeChar.decayMe();
			activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.broadcastUserInfo();
		}
		else if (command.equals("admin_clearteams"))
		{
			World.getInstance().forEachVisibleObject(activeChar, PlayerInstance.class, player ->
			{
				player.setTeam(Team.NONE);
				player.broadcastUserInfo();
			});
		}
		else if (command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();
				int radius = 400;
				if (st.hasMoreTokens())
				{
					radius = Integer.parseInt(st.nextToken());
				}
				Team team = Team.valueOf(val.toUpperCase());
				
				World.getInstance().forEachVisibleObjectInRange(activeChar, PlayerInstance.class, radius, player -> player.setTeam(team));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam_close <none|blue|red> [radius]");
			}
		}
		else if (command.startsWith("admin_setteam"))
		{
			try
			{
				Team team = Team.valueOf(st.nextToken().toUpperCase());
				Creature target = null;
				if (activeChar.getTarget() instanceof Creature)
				{
					target = (Creature) activeChar.getTarget();
				}
				else
				{
					return false;
				}
				target.setTeam(team);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam <none|blue|red>");
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				WorldObject obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null)
					{
						PlayerInstance player = World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
							{
								activeChar.sendMessage(player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								World.getInstance().forEachVisibleObjectInRange(activeChar, WorldObject.class, radius, object -> performSocial(social, object, activeChar));
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performSocial(social, obj, activeChar))
					{
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_ave_abnormal"))
		{
			String param1 = null;
			if (st.countTokens() > 0)
			{
				param1 = st.nextToken();
			}
			
			if ((param1 != null) && !Util.isDigit(param1))
			{
				AbnormalVisualEffect ave;
				
				try
				{
					ave = AbnormalVisualEffect.valueOf(param1);
				}
				catch (Exception e)
				{
					
					return false;
				}
				
				int radius = 0;
				String param2 = null;
				if (st.countTokens() == 1)
				{
					param2 = st.nextToken();
					if (Util.isDigit(param2))
					{
						radius = Integer.parseInt(param2);
					}
				}
				
				if (radius > 0)
				{
					World.getInstance().forEachVisibleObjectInRange(activeChar, WorldObject.class, radius, object -> performAbnormalVisualEffect(ave, object));
					activeChar.sendMessage("Affected all characters in radius " + param2 + " by " + param1 + " abnormal visual effect.");
				}
				else
				{
					final WorldObject obj = activeChar.getTarget() != null ? activeChar.getTarget() : activeChar;
					if (performAbnormalVisualEffect(ave, obj))
					{
						activeChar.sendMessage(obj.getName() + " affected by " + param1 + " abnormal visual effect.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
			}
			else
			{
				int page = 0;
				if (param1 != null)
				{
					try
					{
						page = Integer.parseInt(param1);
					}
					catch (NumberFormatException nfe)
					{
						activeChar.sendMessage("Incorrect page.");
					}
				}
				
				final PageResult result = PageBuilder.newBuilder(AbnormalVisualEffect.values(), 100, "bypass -h admin_ave_abnormal").currentPage(page).style(ButtonsStyle.INSTANCE).bodyHandler((pages, ave, sb) ->
				{
					sb.append(String.format("<button action=\"bypass admin_ave_abnormal %s\" align=left icon=teleport>%s(%d)</button>", ave.name(), ave.name(), ave.getClientId()));
				}).build();
				
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
				html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/ave_abnormal.htm");
				
				if (result.getPages() > 0)
				{
					html.replace("%pages%", "<table width=280 cellspacing=0><tr>" + result.getPagerTemplate() + "</tr></table>");
				}
				else
				{
					html.replace("%pages%", "");
				}
				
				html.replace("%abnormals%", result.getBodyTemplate().toString());
				activeChar.sendPacket(html);
				activeChar.sendMessage("Usage: //" + command.replace("admin_", "") + " <AbnormalVisualEffect> [radius]");
				return true;
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				WorldObject obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				if (obj == null)
				{
					obj = activeChar;
				}
				if (!(obj instanceof Creature))
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
				else
				{
					Creature target = (Creature) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
				
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		else if (command.startsWith("admin_set_displayeffect"))
		{
			WorldObject target = activeChar.getTarget();
			if (!(target instanceof Npc))
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			Npc npc = (Npc) target;
			try
			{
				String type = st.nextToken();
				int diplayeffect = Integer.parseInt(type);
				npc.setState(diplayeffect);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_displayeffect <id>");
			}
		}
		else if (command.startsWith("admin_playmovie"))
		{
			try
			{
				new MovieHolder(Arrays.asList(activeChar), Movie.findByClientId(Integer.parseInt(st.nextToken())));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //playmovie <id>");
			}
		}
		else if (command.startsWith("admin_event_trigger"))
		{
			try
			{
				int triggerId = Integer.parseInt(st.nextToken());
				boolean enable = Boolean.parseBoolean(st.nextToken());
				World.getInstance().forEachVisibleObject(activeChar, PlayerInstance.class, player -> player.sendPacket(new OnEventTrigger(triggerId, enable)));
				activeChar.sendPacket(new OnEventTrigger(triggerId, enable));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //event_trigger id [true | false]");
			}
		}
		else if (command.startsWith("admin_settargetable"))
		{
			activeChar.setTargetable(!activeChar.isTargetable());
		}
		
		if (command.contains("menu"))
		{
			showMainPage(activeChar, command);
		}
		return true;
	}
	
	/**
	 * @param ave the abnormal visual effect
	 * @param target the target
	 * @return {@code true} if target's abnormal state was affected, {@code false} otherwise.
	 */
	private boolean performAbnormalVisualEffect(AbnormalVisualEffect ave, WorldObject target)
	{
		if (target instanceof Creature)
		{
			final Creature character = (Creature) target;
			if (!character.hasAbnormalVisualEffect(ave))
			{
				character.startAbnormalVisualEffect(ave);
			}
			else
			{
				character.stopAbnormalVisualEffect(ave);
			}
			return true;
		}
		return false;
	}
	
	private boolean performSocial(int action, WorldObject target, PlayerInstance activeChar)
	{
		try
		{
			if (target.isCreature())
			{
				if (target instanceof L2ChestInstance)
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target.isNpc()) && ((action < 1) || (action > 20)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target.isPlayer()) && ((action < 2) || ((action > 18) && (action != SocialAction.LEVEL_UP))))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				Creature character = (Creature) target;
				character.broadcastPacket(new SocialAction(character.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	/**
	 * @param type - atmosphere type (signssky,sky)
	 * @param state - atmosphere state(night,day)
	 * @param duration
	 * @param activeChar
	 */
	private void adminAtmosphere(String type, String state, int duration, PlayerInstance activeChar)
	{
		IClientOutgoingPacket packet = null;
		
		if (type.equals("sky"))
		{
			if (state.equals("night"))
			{
				packet = SunSet.STATIC_PACKET;
			}
			else if (state.equals("day"))
			{
				packet = SunRise.STATIC_PACKET;
			}
			else if (state.equals("red"))
			{
				if (duration != 0)
				{
					packet = new ExRedSky(duration);
				}
				else
				{
					packet = new ExRedSky(10);
				}
			}
		}
		else
		{
			activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red> <duration>");
		}
		if (packet != null)
		{
			Broadcast.toAllOnlinePlayers(packet);
		}
	}
	
	private void playAdminSound(PlayerInstance activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(PlayerInstance activeChar, String command)
	{
		String filename = "effects_menu";
		if (command.contains("social"))
		{
			filename = "social";
		}
		AdminHtml.showAdminHtml(activeChar, filename + ".htm");
	}
}
