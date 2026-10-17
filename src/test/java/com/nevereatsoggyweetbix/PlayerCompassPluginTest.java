package com.nevereatsoggyweetbix;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlayerCompassPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayerCompassPlugin.class);
		RuneLite.main(args);
	}
}
