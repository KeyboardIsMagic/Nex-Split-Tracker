package com.nexsplittracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;

@PluginDescriptor(
		name = "Nex Split Tracker",
		description = "Tracks splits for Nex drops",
		tags = {"nex", "loot", "split", "tracker"}
)
public class NexSplitTrackerPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private NexSplitTrackerConfig config;

	private NexSplitTrackerPanel panel;
	private NavigationButton navButton;

	@Provides
	NexSplitTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NexSplitTrackerConfig.class);
	}

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		panel = new NexSplitTrackerPanel(itemManager);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/nex16v2.png");

		navButton = NavigationButton.builder()
				.tooltip("Nex Split Tracker")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	// Add your plugin logic here
}
