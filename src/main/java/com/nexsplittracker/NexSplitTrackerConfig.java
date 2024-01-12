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

}
