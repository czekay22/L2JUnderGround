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
package handlers.effecthandlers;

import org.l2junity.commons.util.Rnd;
import org.l2junity.gameserver.ai.CtrlIntention;
import org.l2junity.gameserver.enums.QuestSound;
import org.l2junity.gameserver.model.L2Seed;
import org.l2junity.gameserver.model.Party;
import org.l2junity.gameserver.model.StatsSet;
import org.l2junity.gameserver.model.actor.Creature;
import org.l2junity.gameserver.model.actor.instance.L2MonsterInstance;
import org.l2junity.gameserver.model.actor.instance.PlayerInstance;
import org.l2junity.gameserver.model.effects.AbstractEffect;
import org.l2junity.gameserver.model.items.instance.ItemInstance;
import org.l2junity.gameserver.model.skills.Skill;
import org.l2junity.gameserver.network.client.send.SystemMessage;
import org.l2junity.gameserver.network.client.send.string.SystemMessageId;

/**
 * Sow effect implementation.
 * @author Adry_85, l3x
 */
public final class Sow extends AbstractEffect
{
	public Sow(StatsSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		if (!effector.isPlayer() || !effected.isMonster())
		{
			return;
		}
		
		final PlayerInstance player = effector.getActingPlayer();
		final L2MonsterInstance target = (L2MonsterInstance) effected;
		
		if (target.isDead() || (!target.getTemplate().canBeSown()) || target.isSeeded() || (target.getSeederId() != player.getObjectId()))
		{
			return;
		}
		
		// Consuming used seed
		final L2Seed seed = target.getSeed();
		if (!player.destroyItemByItemId("Consume", seed.getSeedId(), 1, target, false))
		{
			return;
		}
		
		final SystemMessage sm;
		if (calcSuccess(player, target, seed))
		{
			player.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
			target.setSeeded(player.getActingPlayer());
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
		}
		
		Party party = player.getParty();
		if (party != null)
		{
			party.broadcastPacket(sm);
		}
		else
		{
			player.sendPacket(sm);
		}
		
		// TODO: Mob should not aggro on player, this way doesn't work really nice
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}
	
	private static boolean calcSuccess(Creature activeChar, Creature target, L2Seed seed)
	{
		// TODO: check all the chances
		final int minlevelSeed = seed.getLevel() - 5;
		final int maxlevelSeed = seed.getLevel() + 5;
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		int basicSuccess = seed.isAlternative() ? 20 : 90;
		
		// seed level
		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}
		
		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}
		
		// chance can't be less than 1%
		Math.max(basicSuccess, 1);
		return Rnd.nextInt(99) < basicSuccess;
	}
}
