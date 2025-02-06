package com.osrs_splits;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("nexsplittracker")
public interface OsrsSplitsConfig extends Config
{
	@ConfigSection(
			name = "API Configuration",
			description = "Settings for managing the API key",
			position = 0
	)
	String apiConfigSection = "apiConfig";

	@ConfigItem(
			keyName = "apiKey",
			name = "API Key",
			description = "Enter your API key here (hidden for security)",
			secret = true,
			position = 1,
			section = apiConfigSection
	)
	String apiKey();

	@ConfigItem(
			keyName = "saveApiKey",
			name = "Save API Key",
			description = "Click to save or overwrite the API key file.",
			position = 2,
			section = apiConfigSection
	)
	default boolean saveApiKey() {
		return false; // Default to false, acts as trigger
	}

	@ConfigItem(
			keyName = "increaseDropRate",
			name = "Increase Drop Rate",
			description = "Might do something if you really believe",
			position = 3
	)
	default boolean dropRateBoost() {
		return false;
	}

	@ConfigItem(
			keyName = "enableExternalSharing",
			name = "Enable External Sharing",
			warning = "<html><b>WARNING:</b> Enabling this will send your data (screenshots, party info) to an external server. Use at your own risk.</html>",
			position = 4,
            description = ""
	)
	default boolean enableExternalSharing() { return false; }
}
