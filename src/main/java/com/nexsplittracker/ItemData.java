package com.nexsplittracker;

import java.time.LocalDate;

class ItemData
{
    // Fields
    private String itemName;
    private double splitAmount;
    private LocalDate date;
    private int teamSize;
    private boolean isReceived;


    // Ctor
    public ItemData(String itemName, double splitAmount, LocalDate date, int teamSize, boolean isReceived)
    {
        this.itemName = itemName;
        this.splitAmount = splitAmount;
        this.date = date;
        this.teamSize = teamSize;
        this.isReceived = isReceived;
    }



    // Getters
    public String getItemName()
    {
        return itemName;
    }

    public double getSplitAmount()
    {
        return splitAmount;
    }

    public LocalDate getDate()
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



    // Setters
    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public void setSplitAmount(double splitAmount)
    {
        this.splitAmount = splitAmount;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public void setTeamSize(int teamSize)
    {
        this.teamSize = teamSize;
    }

    public void setReceived(boolean received)
    {
        isReceived = received;
    }



    //toString method for debugging
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

