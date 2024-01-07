package com.nexsplittracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NexSplitTrackerPlugin.class);
		RuneLite.main(args);
	}
}