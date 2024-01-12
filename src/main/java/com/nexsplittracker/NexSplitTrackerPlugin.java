package com.nexsplittracker;

import com.google.gson.Gson;
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
		description = "Track Nex uniques and GP split among teams"
)
public class NexSplitTrackerPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private NexSplitTrackerConfig config;

	@Inject
	private Gson gson;

	@Inject
	private ItemManager itemManager;


	private NexSplitTrackerPanel panel;
	private NavigationButton navButton;

    @Provides
	NexSplitTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NexSplitTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = new NexSplitTrackerPanel(itemManager, gson);

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

}
