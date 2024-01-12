package com.nexsplittracker;

class ItemData
{
    private String itemName;
    private double splitAmount;
    private String date;
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


    public String getItemName()
    {
        return itemName;
    }

    public double getSplitAmount()
    {
        return splitAmount;
    }

    public String getDate()
    {
        return date;
    }

    public int getTeamSize()
    {
        return teamSize;
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

