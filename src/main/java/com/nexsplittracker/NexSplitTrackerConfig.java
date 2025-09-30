package com.nexsplittracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("nexsplittracker")
public interface NexSplitTrackerConfig extends Config
{
	// Example configuration item
	@ConfigItem(
			keyName = "increaseDropRate",
			name = "Increase Drop Rate",
			description = "Might do something if you really believe"
	)
	default boolean dropRateBoost()
	{
		return false;
	}

	@ConfigItem(
			keyName = "showKillCount",
			name = "Show Kill Count",
			description = "Display kill count instead of date in the details table"
	)
	default boolean showKillCount()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableKillCountEntry",
			name = "Enable Kill Count Entry",
			description = "Show kill count input field when adding drops"
	)
	default boolean enableKillCountEntry()
	{
		return true;
	}

}
