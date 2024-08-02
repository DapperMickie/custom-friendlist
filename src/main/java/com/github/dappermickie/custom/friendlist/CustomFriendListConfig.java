package com.github.dappermickie.custom.friendlist;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("odablockdmmfinale")
public interface CustomFriendListConfig extends Config
{

	@ConfigItem(
		position = 2,
		keyName = "hidePlayers",
		name = "Hide Others",
		description = "Configures whether or not other players are hidden"
	)
	default boolean hideOthers()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "hidePlayers2D",
		name = "Hide Others 2D",
		description = "Configures whether or not other players 2D elements are hidden"
	)
	default boolean hideOthers2D()
	{
		return true;
	}

	@ConfigSection(
		name = "Highlight Options",
		description = "Toggle highlighted players by type (self, friends, etc.) and choose their highlight colors",
		position = 99
	)
	String highlightSection = "section";

	enum HighlightSetting
	{
		DISABLED,
		ENABLED,
		PVP;
	}

	@ConfigItem(
		position = 6,
		keyName = "highlightFriendsChat",
		name = "Highlight custom friends",
		description = "Configures whether custom friends should be highlighted",
		section = highlightSection
	)
	default HighlightSetting highlightFriendsChat()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 7,
		keyName = "friendsChatColor",
		name = "Custom Friends (PI)",
		description = "Color of friends chat members",
		section = highlightSection
	)
	default Color getFriendsChatMemberColor()
	{
		return new Color(170, 0, 255);
	}

	@ConfigItem(
		position = 10,
		keyName = "drawPlayerTiles",
		name = "Draw tiles under players",
		description = "Configures whether or not tiles under highlighted players should be drawn"
	)
	default boolean drawTiles()
	{
		return false;
	}

	@ConfigItem(
		position = 11,
		keyName = "playerNamePosition",
		name = "Name position",
		description = "Configures the position of drawn player names, or if they should be disabled"
	)
	default PlayerNameLocation playerNamePosition()
	{
		return PlayerNameLocation.ABOVE_HEAD;
	}

	@ConfigItem(
		position = 12,
		keyName = "drawMinimapNames",
		name = "Draw names on minimap",
		description = "Configures whether or not minimap names for players with rendered names should be drawn"
	)
	default boolean drawMinimapNames()
	{
		return false;
	}
}
