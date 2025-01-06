package com.osrs_splits;

import com.osrs_splits.LootTracker.NexSplitTrackerPanel;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;

public class OsrsSplitMasterPanel extends PluginPanel
{
    private final OsrsSplitPlugin plugin;
    private final OsrsSplitPluginPanel partyManagerPanel;
    private final NexSplitTrackerPanel lootTrackerPanel;

    public OsrsSplitMasterPanel(OsrsSplitPlugin plugin, OsrsSplitPluginPanel partyManagerPanel, NexSplitTrackerPanel lootTrackerPanel)
    {
        this.plugin = plugin;
        this.partyManagerPanel = partyManagerPanel;
        this.lootTrackerPanel = lootTrackerPanel;

        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("OSRS Splits - The Kodai", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        //Create tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Loot Tracker", lootTrackerPanel);

        JPanel pmWrapper = new JPanel(new BorderLayout());
        pmWrapper.add(partyManagerPanel, BorderLayout.NORTH);
        tabbedPane.addTab("Party Manager", pmWrapper);

        //tabbedPane.addTab("Party Manager", partyManagerPanel);
        add(tabbedPane, BorderLayout.CENTER);
    }
}
