package com.nexsplittracker;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("nexsplittracker")
public interface NexSplitTrackerConfig extends Config
{
	// Example configuration item
	@ConfigItem(
			keyName = "exampleSetting",
			name = "Example Setting",
			description = "This is an example configuration setting."
	)
	default boolean exampleSetting()
	{
		return true;
	}

	// Add other configuration settings here
}
