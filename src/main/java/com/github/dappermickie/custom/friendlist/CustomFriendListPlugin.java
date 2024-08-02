package com.github.dappermickie.custom.friendlist;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import static net.runelite.api.MenuAction.ITEM_USE_ON_PLAYER;
import static net.runelite.api.MenuAction.PLAYER_EIGHTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FIFTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FIRST_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FOURTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SECOND_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SEVENTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SIXTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_THIRD_OPTION;
import static net.runelite.api.MenuAction.RUNELITE_PLAYER;
import static net.runelite.api.MenuAction.WALK;
import static net.runelite.api.MenuAction.WIDGET_TARGET_ON_PLAYER;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@PluginDescriptor(
	name = "Custom Friendlist"
)
public class CustomFriendListPlugin extends Plugin
{
	@Inject
	private Client client;

	/**
	 * CUSTOM FRIEND LIST -- ENTITY HIDER
	 **/

	@Inject
	private CustomFriendListConfig config;

	@Inject
	private Hooks hooks;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
	private boolean hideOthers;
	private boolean hideOthers2D;

	/**
	 * CUSTOM FRIEND LIST -- PLAYER INDICATOR
	 **/

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CustomFriendListIndicatorsOverlay playerIndicatorsOverlay;

	@Inject
	private CustomFriendListIndicatorsTileOverlay playerIndicatorsTileOverlay;

	@Inject
	private CustomFriendListIndicatorsMinimapOverlay playerIndicatorsMinimapOverlay;

	@Inject
	private CustomFriendListIndicatorsService playerIndicatorsService;

	@Inject
	private ClientThread clientThread;
	private static final String TRADING_WITH_TEXT = "Trading with: ";


	@Override
	protected void startUp()
	{
		// Entity hider
		updateConfig();

		hooks.registerRenderableDrawListener(drawListener);

		// Player indicator
		overlayManager.add(playerIndicatorsOverlay);
		overlayManager.add(playerIndicatorsTileOverlay);
		overlayManager.add(playerIndicatorsMinimapOverlay);
	}

	@Override
	protected void shutDown()
	{
		// Entity Hider
		hooks.unregisterRenderableDrawListener(drawListener);

		// Player indicator
		overlayManager.remove(playerIndicatorsOverlay);
		overlayManager.remove(playerIndicatorsTileOverlay);
		overlayManager.remove(playerIndicatorsMinimapOverlay);
	}

	private void updateConfig()
	{
		String fromConfig = config.customFriendlist();
		playerList = fromConfig.split("\n");

		hideOthers = config.hideOthers();
		hideOthers2D = config.hideOthers2D();
	}

	String[] playerList = new String[]{"bald", "jutsi", "Don Huono"};

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof Player)
		{
			Player player = (Player) renderable;
			Player local = client.getLocalPlayer();

			if (player.getName() == null)
			{
				// player.isFriend() and player.isFriendsChatMember() npe when the player has a null name
				return true;
			}

			// Allow hiding local self in pvp, which is an established meta.
			// It is more advantageous than renderself due to being able to still render local player 2d
			if (player == local)
			{
				return true;
			}

			if (isPlayerInList(player.getName()))
			{
				return !(drawingUI ? hideOthers2D : hideOthers);
			}

			return true;
		}

		return true;
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (client.isMenuOpen())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		for (MenuEntry entry : menuEntries)
		{
			MenuAction type = entry.getType();

			if (type == WALK
				|| type == WIDGET_TARGET_ON_PLAYER
				|| type == ITEM_USE_ON_PLAYER
				|| type == PLAYER_FIRST_OPTION
				|| type == PLAYER_SECOND_OPTION
				|| type == PLAYER_THIRD_OPTION
				|| type == PLAYER_FOURTH_OPTION
				|| type == PLAYER_FIFTH_OPTION
				|| type == PLAYER_SIXTH_OPTION
				|| type == PLAYER_SEVENTH_OPTION
				|| type == PLAYER_EIGHTH_OPTION
				|| type == RUNELITE_PLAYER)
			{
				Player[] players = client.getCachedPlayers();
				Player player = null;

				int identifier = entry.getIdentifier();

				// 'Walk here' identifiers are offset by 1 because the default
				// identifier for this option is 0, which is also a player index.
				if (type == WALK)
				{
					identifier--;
				}

				if (identifier >= 0 && identifier < players.length)
				{
					player = players[identifier];
				}

				if (player == null)
				{
					continue;
				}

				CustomFriendListIndicatorsService.Decorations decorations = playerIndicatorsService.getDecorations(player);
				if (decorations == null)
				{
					continue;
				}

				String oldTarget = entry.getTarget();
				String newTarget = decorateTarget(oldTarget, decorations);

				entry.setTarget(newTarget);
			}
		}
	}

	@VisibleForTesting
	String decorateTarget(String oldTarget, CustomFriendListIndicatorsService.Decorations decorations)
	{
		String newTarget = oldTarget;

		if (decorations.getColor() != null)
		{
			String prefix = "";
			int idx = oldTarget.indexOf("->");
			if (idx != -1)
			{
				prefix = oldTarget.substring(0, idx + 3); // <col=ff9040>Earth rune</col><col=ff> ->
				oldTarget = oldTarget.substring(idx + 3);
			}

			// <col=ff0000>title0RuneLitetitle1<col=ff>  (level-126)title2
			idx = oldTarget.indexOf('>');
			// remove leading <col>
			oldTarget = oldTarget.substring(idx + 1);

			newTarget = prefix + ColorUtil.prependColorTag(oldTarget, decorations.getColor());
		}

		return newTarget;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.TRADE_MAIN_INIT)
		{
			clientThread.invokeLater(() ->
			{
				Widget tradeTitle = client.getWidget(ComponentID.TRADE_HEADER);
				String header = tradeTitle.getText();
				String playerName = header.substring(TRADING_WITH_TEXT.length());

				Player targetPlayer = findPlayer(playerName);
				if (targetPlayer == null)
				{
					return;
				}

				CustomFriendListIndicatorsService.Decorations decorations = playerIndicatorsService.getDecorations(targetPlayer);
				if (decorations != null && decorations.getColor() != null)
				{
					tradeTitle.setText(TRADING_WITH_TEXT + ColorUtil.wrapWithColorTag(playerName, decorations.getColor()));
				}
			});
		}
	}

	public boolean isPlayerInList(String name)
	{
		return Arrays.stream(playerList).anyMatch(x->x.split(" - ")[0].equals(name));
	}

	private Player findPlayer(String name)
	{
		for (Player player : client.getPlayers())
		{
			if (player.getName().equals(name))
			{
				return player;
			}
		}
		return null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals("customfriendlist"))
		{
			updateConfig();
		}
	}

	@Provides
	CustomFriendListConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomFriendListConfig.class);
	}
}
