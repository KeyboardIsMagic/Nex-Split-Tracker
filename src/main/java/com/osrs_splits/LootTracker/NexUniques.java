package com.osrs_splits.LootTracker;

import net.runelite.api.ItemID;
import java.util.Arrays;

public enum NexUniques
{
    ZVAMBS("Zaryte Vambraces", "Vambs", ItemID.ZARYTE_VAMBRACES),
    NIHIL_HORN("Nihil Horn", "Horn", ItemID.NIHIL_HORN),
    TORVA_HELM("Torva Full Helm", "Helm", ItemID.TORVA_FULL_HELM),
    TORVA_PLATE("Torva Platebody", "Plate", ItemID.TORVA_PLATEBODY),
    TORVA_LEGS("Torva Platelegs", "Legs", ItemID.TORVA_PLATELEGS),
    ANCIENT_HILT("Ancient Hilt", "Hilt", ItemID.ANCIENT_HILT);

    private final String fullName;
    private final String shortName;
    private final int itemId;

    NexUniques(String fullName, String shortName, int itemId)
    {
        this.fullName = fullName;
        this.shortName = shortName;
        this.itemId = itemId;
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public int getItemId()
    {
        return itemId;
    }

    // Static method to get names array
    public static String[] names()
    {
        return Arrays.stream(NexUniques.values()).map(NexUniques::getFullName).toArray(String[]::new);
    }

}