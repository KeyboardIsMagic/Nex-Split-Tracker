package com.nexsplittracker;

import com.google.gson.Gson;
import net.runelite.client.RuneLite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager
{
    private static final Logger logger = Logger.getLogger(DataManager.class.getName());
    private static final File PLUGIN_DIR = new File(RuneLite.RUNELITE_DIR, "NexSplitTracker");
    private static final String DATA_FILE_NAME = "data.json";

    private final Gson gson;
    private final DataMigrationUtil migrationUtil;
    private List<ItemDataV2> itemDataList;

    public DataManager(Gson gson)
    {
        this.gson = gson;
        this.migrationUtil = new DataMigrationUtil(gson);
        this.itemDataList = new ArrayList<>();

        createPluginDirectory();
    }

    public void loadData()
    {
        try
        {
            String dataFilePath = getDataFilePath();
            itemDataList = migrationUtil.loadData(dataFilePath);
            logger.info("Loaded " + itemDataList.size() + " items from data file");
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error loading data file", e);
            itemDataList = new ArrayList<>();
        }
    }

    public void saveData()
    {
        try
        {
            String dataFilePath = getDataFilePath();
            migrationUtil.saveData(itemDataList, dataFilePath);
            logger.info("Saved " + itemDataList.size() + " items to data file");
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error saving data file", e);
        }
    }

    public void addItem(ItemDataV2 item)
    {
        itemDataList.add(item);
        saveData();
    }

    public void removeItem(ItemDataV2 item)
    {
        itemDataList.remove(item);
        saveData();
    }


    public void updateItem(ItemDataV2 oldItem, ItemDataV2 newItem)
    {
        int index = itemDataList.indexOf(oldItem);
        if (index != -1)
        {
            itemDataList.set(index, newItem);
            saveData();
        }
    }

    public ItemDataV2 findItemByTableRowData(String displayName, double splitAmount, String dateOrKillCount, int teamSize, NexSplitTrackerConfig config)
    {
        // Convert display name back to full name
        String fullName = convertDisplayNameToFullName(displayName);

        for (ItemDataV2 item : itemDataList)
        {
            if (item.getItemName().equals(fullName) &&
                item.getSplitAmount() == splitAmount &&
                item.getTeamSize() == teamSize)
            {
                // Check date/kill count match based on current display mode
                if (config.showKillCount())
                {
                    // Comparing kill count
                    if (item.hasKillCount() && item.getKillCount().toString().equals(dateOrKillCount))
                    {
                        return item;
                    }
                    else if (!item.hasKillCount() && "N/A".equals(dateOrKillCount))
                    {
                        return item;
                    }
                }
                else
                {
                    // Comparing date
                    if (item.getDate().equals(dateOrKillCount))
                    {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    private String convertDisplayNameToFullName(String displayName)
    {
        for (NexUniques unique : NexUniques.values())
        {
            if (unique.getShortName().equals(displayName))
            {
                return unique.getFullName();
            }
        }
        return displayName; // Fallback
    }

    public List<ItemDataV2> getAllItems()
    {
        return new ArrayList<>(itemDataList); // Return copy to prevent external modification
    }


    private void createPluginDirectory()
    {
        if (!PLUGIN_DIR.exists())
        {
            boolean created = PLUGIN_DIR.mkdirs();
            if (created)
            {
                logger.info("Created plugin directory: " + PLUGIN_DIR.getAbsolutePath());
            }
            else
            {
                logger.warning("Failed to create plugin directory: " + PLUGIN_DIR.getAbsolutePath());
            }
        }
    }

    private String getDataFilePath()
    {
        return new File(PLUGIN_DIR, DATA_FILE_NAME).getAbsolutePath();
    }

}