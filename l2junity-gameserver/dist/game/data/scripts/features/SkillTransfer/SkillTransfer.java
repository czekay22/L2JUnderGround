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
package features.SkillTransfer;

import org.l2junity.Config;
import org.l2junity.gameserver.data.xml.impl.ClassListData;
import org.l2junity.gameserver.data.xml.impl.SkillTreesData;
import org.l2junity.gameserver.enums.IllegalActionPunishmentType;
import org.l2junity.gameserver.model.PcCondOverride;
import org.l2junity.gameserver.model.SkillLearn;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.actor.transform.Transform;
import org.l2junity.gameserver.model.events.impl.character.player.OnPlayerProfessionChange;
import org.l2junity.gameserver.model.holders.ItemHolder;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Skill Transfer feature.
 * @author Zoey76
 */
public final class SkillTransfer extends AbstractNpcAI
{
	private static final String HOLY_POMANDER = "HOLY_POMANDER_";
	private static final ItemHolder[] PORMANDERS =
	{
		// Cardinal (97)
		new ItemHolder(15307, 7),
		// Eva's Saint (105)
		new ItemHolder(15308, 7),
		// Shillen Saint (112)
		new ItemHolder(15309, 7)
	};
	
	private SkillTransfer()
	{
		setPlayerProfessionChangeId(this::onProfessionChange);
		setOnEnterWorld(Config.SKILL_CHECK_ENABLE);
	}
	
	public void onProfessionChange(OnPlayerProfessionChange event)
	{
		final PlayerInstance player = event.getActiveChar();
		final int index = getTransferClassIndex(player);
		if (index < 0)
		{
			return;
		}
		
		final String name = HOLY_POMANDER + player.getClassId().getId();
		if (!player.getVariables().getBoolean(name, false))
		{
			player.getVariables().set(name, true);
			giveItems(player, PORMANDERS[index]);
		}
	}
	
	@Override
	public String onEnterWorld(PlayerInstance player)
	{
		if (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM)
		{
			final int index = getTransferClassIndex(player);
			if (index < 0)
			{
				return super.onEnterWorld(player);
			}
			long count = PORMANDERS[index].getCount() - player.getInventory().getInventoryItemCount(PORMANDERS[index].getId(), -1, false);
			for (Skill sk : player.getAllSkills())
			{
				for (SkillLearn s : SkillTreesData.getInstance().getTransferSkillTree(player.getClassId()).values())
				{
					if (s.getSkillId() == sk.getId())
					{
						// Holy Weapon allowed for Shilien Saint/Inquisitor stance
						if ((sk.getId() == 1043) && (index == 2) && player.checkTransformed(Transform::isStance))
						{
							continue;
						}
						
						count--;
						if (count < 0)
						{
							final String className = ClassListData.getInstance().getClass(player.getClassId()).getClassName();
							Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has too many transfered skills or items, skill:" + s.getName() + " (" + sk.getId() + "/" + sk.getLevel() + "), class:" + className, IllegalActionPunishmentType.BROADCAST);
							if (Config.SKILL_CHECK_REMOVE)
							{
								player.removeSkill(sk);
							}
						}
					}
				}
			}
		}
		return super.onEnterWorld(player);
	}
	
	private static int getTransferClassIndex(PlayerInstance player)
	{
		switch (player.getClassId())
		{
			case CARDINAL:
			{
				return 0;
			}
			case EVA_SAINT:
			{
				return 1;
			}
			case SHILLIEN_SAINT:
			{
				return 2;
			}
			default:
			{
				return -1;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new SkillTransfer();
	}
}
