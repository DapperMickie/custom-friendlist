package com.github.dappermickie.custom.friendlist;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CustomFriendListPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CustomFriendListPlugin.class);
		RuneLite.main(args);
	}
}