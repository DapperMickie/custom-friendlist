package com.github.dappermickie.custom.friendlist;
/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.client.party.PartyService;

@Singleton
class CustomFriendListIndicatorsService
{
	private final Client client;
	private final CustomFriendListConfig config;
	private final PartyService partyService;
	private final CustomFriendListPlugin plugin;

	@Inject
	private CustomFriendListIndicatorsService(Client client, CustomFriendListConfig config, PartyService partyService, CustomFriendListPlugin plugin)
	{
		this.config = config;
		this.client = client;
		this.partyService = partyService;
		this.plugin = plugin;
	}

	void forEachPlayer(final BiConsumer<Player, Decorations> consumer)
	{
		for (Player player : client.getPlayers())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}

			Decorations decorations = getDecorations(player);
			if (decorations != null && decorations.getColor() != null)
			{
				consumer.accept(player, decorations);
			}
		}
	}

	Decorations getDecorations(Player player)
	{
		if (player.getName() == null)
		{
			return null;
		}

		final Predicate<CustomFriendListConfig.HighlightSetting> isEnabled = (hs) -> hs == CustomFriendListConfig.HighlightSetting.ENABLED ||
			(hs == CustomFriendListConfig.HighlightSetting.PVP && (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1 || client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1));

		Color color = null;
		if (isEnabled.test(config.highlightFriendsChat()) && plugin.isPlayerInList(player.getName()))
		{
			color = config.getFriendsChatMemberColor();
		}

		if (color == null)
		{
			return null;
		}

		return new Decorations(color);
	}

	@Value
	static class Decorations
	{
		Color color;
	}
}