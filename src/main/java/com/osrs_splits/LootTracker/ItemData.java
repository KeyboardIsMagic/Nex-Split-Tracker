package com.osrs_splits.LootTracker;

import lombok.Getter;

class ItemData
{
    @Getter
    private String itemName;
    @Getter
    private double splitAmount;
    @Getter
    private String date;
    @Getter
    private int teamSize;
    private boolean isReceived;



    public ItemData(String itemName, double splitAmount, String date, int teamSize, boolean isReceived)
    {
        this.itemName = itemName;
        this.splitAmount = splitAmount;
        this.date = date;
        this.teamSize = teamSize;
        this.isReceived = isReceived;
    }


    public boolean isReceived()
    {
        return isReceived;
    }



    @Override
    public String toString()
    {
        return "ItemData{" +
                "itemName='" + itemName + '\'' +
                ", splitAmount=" + splitAmount +
                ", date=" + date +
                ", teamSize=" + teamSize +
                '}';
    }



}
