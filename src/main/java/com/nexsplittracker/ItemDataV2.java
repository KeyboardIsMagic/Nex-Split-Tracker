package com.nexsplittracker;

public class ItemDataV2
{
    private String itemName;
    private double splitAmount;
    private String date;
    private int teamSize;
    private boolean isReceived;
    private Integer killCount; // null for legacy data

    public ItemDataV2(String itemName, double splitAmount, String date, int teamSize, boolean isReceived)
    {
        this.itemName = itemName;
        this.splitAmount = splitAmount;
        this.date = date;
        this.teamSize = teamSize;
        this.isReceived = isReceived;
        this.killCount = null; // Default to null for backward compatibility
    }

    public ItemDataV2(String itemName, double splitAmount, String date, int teamSize, boolean isReceived, Integer killCount)
    {
        this.itemName = itemName;
        this.splitAmount = splitAmount;
        this.date = date;
        this.teamSize = teamSize;
        this.isReceived = isReceived;
        this.killCount = killCount;
    }

    // Constructor for migration from ItemData
    public ItemDataV2(ItemData oldData)
    {
        this.itemName = oldData.getItemName();
        this.splitAmount = oldData.getSplitAmount();
        this.date = oldData.getDate();
        this.teamSize = oldData.getTeamSize();
        this.isReceived = oldData.isReceived();
        this.killCount = null; // Legacy data won't have kill count
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public double getSplitAmount()
    {
        return splitAmount;
    }

    public void setSplitAmount(double splitAmount)
    {
        this.splitAmount = splitAmount;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public int getTeamSize()
    {
        return teamSize;
    }

    public void setTeamSize(int teamSize)
    {
        this.teamSize = teamSize;
    }

    public boolean isReceived()
    {
        return isReceived;
    }

    public void setReceived(boolean received)
    {
        isReceived = received;
    }

    public Integer getKillCount()
    {
        return killCount;
    }

    public void setKillCount(Integer killCount)
    {
        this.killCount = killCount;
    }

    public boolean hasKillCount()
    {
        return killCount != null;
    }

    @Override
    public String toString()
    {
        return "ItemDataV2{" +
                "itemName='" + itemName + '\'' +
                ", splitAmount=" + splitAmount +
                ", date='" + date + '\'' +
                ", teamSize=" + teamSize +
                ", isReceived=" + isReceived +
                ", killCount=" + killCount +
                '}';
    }
}