package com.nexsplittracker;

import net.runelite.api.ItemID;

public enum NexUniques {
    ZVAMBS("Zaryte Vambraces", ItemID.ZARYTE_VAMBRACES),
    NIHIL_HORN("Nihil Horn", ItemID.NIHIL_HORN),
    TORVA_HELM("Torva Full Helm", ItemID.TORVA_FULL_HELM),
    TORVA_PLATE("Torva Platebody", ItemID.TORVA_PLATEBODY),
    TORVA_LEGS("Torva Platelegs", ItemID.TORVA_PLATELEGS),
    ANCIENT_HILT("Ancient Hilt", ItemID.ANCIENT_HILT);

    private final String name;
    private final int itemId;

    NexUniques(String name, int itemId)
    {
        this.name = name;
        this.itemId = itemId;
    }

    public String getName()
    {
        return name;
    }

    public int getItemId()
    {
        return itemId;
    }

}

