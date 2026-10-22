package com.nevereatsoggyweetbix;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NeverEatSoggyWeetbixDevClient
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NeverEatSoggyWeetbixPlugin.class);
		RuneLite.main(args);
	}
}
