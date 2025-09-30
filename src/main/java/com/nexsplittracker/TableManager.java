package com.nexsplittracker;

import net.runelite.client.game.ItemManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableManager
{
    private final ItemManager itemManager;
    private final NexSplitTrackerConfig config;

    // Primary table (overview)
    private DefaultTableModel primaryTableModel;
    private JTable primaryTable;

    // Secondary table (details)
    private DefaultTableModel secondaryTableModel;
    private JTable secondaryTable;

    public TableManager(ItemManager itemManager, NexSplitTrackerConfig config)
    {
        this.itemManager = itemManager;
        this.config = config;
    }

    public JTable initializePrimaryTable()
    {
        String[] columnNames = {"Drop", "Received", "Seen", "Split"};
        primaryTableModel = new DefaultTableModel(columnNames, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        primaryTable = new JTable(primaryTableModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        primaryTable.setFillsViewportHeight(true);
        primaryTable.setDefaultEditor(Object.class, null);
        primaryTable.setRowHeight(30);
        primaryTable.setShowGrid(true);
        primaryTable.setGridColor(Color.BLACK);
        primaryTable.setShowHorizontalLines(true);
        primaryTable.setShowVerticalLines(false);

        primaryTable.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        primaryTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        primaryTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        primaryTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Add header row
        primaryTableModel.addRow(new Object[]{"Drop", "Received", "Seen", "Split"});

        // Add rows for each unique item
        for (NexUniques unique : NexUniques.values())
        {
            ImageIcon icon = new ImageIcon(itemManager.getImage(unique.getItemId()));
            icon.setDescription(unique.getFullName());
            Object[] row = {icon, 0, 0, 0.0};
            primaryTableModel.addRow(row);
        }

        // Add totals row
        primaryTableModel.addRow(new Object[]{"Total", 0, 0, 0});

        return primaryTable;
    }

    public JTable initializeSecondaryTable()
    {
        String[] detailColumnNames = getSecondaryTableColumnNames();
        secondaryTableModel = new DefaultTableModel(detailColumnNames, 0);
        secondaryTable = new JTable(secondaryTableModel);
        secondaryTable.setTableHeader(new ToolTipHeader(secondaryTable.getColumnModel()));
        secondaryTable.setToolTipText("Double-click to edit an entry, right-click for options");

        // Center cell text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Set the renderer to each column
        for (int i = 0; i < secondaryTableModel.getColumnCount(); i++)
        {
            secondaryTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Schedule the adjustment of column widths
        SwingUtilities.invokeLater(this::adjustSecondaryTableColumnWidths);

        return secondaryTable;
    }

    private String[] getSecondaryTableColumnNames()
    {
        if (config.showKillCount())
        {
            return new String[]{"Item", "Split", "Kill Count", "Team", "Drop"};
        }
        else
        {
            return new String[]{"Item", "Split", "Date", "Team", "Drop"};
        }
    }

    public void updatePrimaryTable(List<ItemDataV2> allItems)
    {
        resetPrimaryTableData();

        // Aggregate counts and splits for each unique item
        Map<String, ItemAggregatedData> aggregatedDataMap = new HashMap<>();
        for (ItemDataV2 item : allItems)
        {
            String itemName = item.getItemName();
            aggregatedDataMap.putIfAbsent(itemName, new ItemAggregatedData());
            ItemAggregatedData aggregatedData = aggregatedDataMap.get(itemName);

            if (item.isReceived())
            {
                aggregatedData.increaseReceived();
            }
            else
            {
                aggregatedData.increaseSeen();
            }
            aggregatedData.increaseSplit(item.getSplitAmount());
        }

        // Update the primary table with aggregated data
        for (Map.Entry<String, ItemAggregatedData> entry : aggregatedDataMap.entrySet())
        {
            String itemName = entry.getKey();
            ItemAggregatedData aggregatedData = entry.getValue();

            int index = findRowIndexByItemName(itemName);
            if (index != -1)
            {
                primaryTableModel.setValueAt(aggregatedData.getReceivedCount(), index, 1);
                primaryTableModel.setValueAt(aggregatedData.getSeenCount(), index, 2);
                primaryTableModel.setValueAt(aggregatedData.getTotalSplit(), index, 3);
            }
        }

        updateTotals();
    }

    public void updateSecondaryTable(List<ItemDataV2> allItems)
    {
        // Check if column structure needs to change
        String[] expectedColumns = getSecondaryTableColumnNames();
        String[] currentColumns = new String[secondaryTableModel.getColumnCount()];
        for (int i = 0; i < currentColumns.length; i++)
        {
            currentColumns[i] = secondaryTableModel.getColumnName(i);
        }

        boolean needsRestructure = false;
        if (expectedColumns.length != currentColumns.length)
        {
            needsRestructure = true;
        }
        else
        {
            for (int i = 0; i < expectedColumns.length; i++)
            {
                if (!expectedColumns[i].equals(currentColumns[i]))
                {
                    needsRestructure = true;
                    break;
                }
            }
        }

        if (needsRestructure)
        {
            // Rebuild table with new column structure
            secondaryTableModel.setColumnIdentifiers(expectedColumns);
        }

        secondaryTableModel.setRowCount(0); // Clear the table

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        for (ItemDataV2 item : allItems)
        {
            String drop = item.isReceived() ? "R" : "S";
            String displayName = getShortNameForItem(item.getItemName());

            Object[] rowData;
            if (config.showKillCount())
            {
                String killCountValue = item.hasKillCount() ? item.getKillCount().toString() : "N/A";
                rowData = new Object[]{
                        displayName,
                        item.getSplitAmount(),
                        killCountValue,
                        item.getTeamSize(),
                        drop
                };
            }
            else
            {
                LocalDate date = LocalDate.parse(item.getDate(), formatter);
                String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd/yy"));
                rowData = new Object[]{
                        displayName,
                        item.getSplitAmount(),
                        formattedDate,
                        item.getTeamSize(),
                        drop
                };
            }

            secondaryTableModel.addRow(rowData);
        }

        if (needsRestructure)
        {
            adjustSecondaryTableColumnWidths();
        }
    }

    private void resetPrimaryTableData()
    {
        // Reset the counts and splits for all items (skip header and total rows)
        for (int i = 1; i < primaryTableModel.getRowCount() - 1; i++)
        {
            primaryTableModel.setValueAt(0, i, 1); // Reset 'Received' count
            primaryTableModel.setValueAt(0, i, 2); // Reset 'Seen' count
            primaryTableModel.setValueAt(0.0, i, 3); // Reset 'Split' value
        }
    }

    private void updateTotals()
    {
        int totalReceived = 0;
        int totalSeen = 0;
        double totalSplit = 0.0;

        // Calculate totals (skip header and total rows)
        for (int i = 1; i < primaryTableModel.getRowCount() - 1; i++)
        {
            totalReceived += (Integer) primaryTableModel.getValueAt(i, 1);
            totalSeen += (Integer) primaryTableModel.getValueAt(i, 2);
            totalSplit += (Double) primaryTableModel.getValueAt(i, 3);
        }

        int lastRowIndex = primaryTableModel.getRowCount() - 1;
        primaryTableModel.setValueAt("Total Drops", lastRowIndex, 0);
        primaryTableModel.setValueAt(totalReceived, lastRowIndex, 1);
        primaryTableModel.setValueAt(totalSeen, lastRowIndex, 2);
        primaryTableModel.setValueAt(totalSplit, lastRowIndex, 3);
    }

    private int findRowIndexByItemName(String itemName)
    {
        for (int i = 0; i < primaryTableModel.getRowCount(); i++)
        {
            Object cellValue = primaryTableModel.getValueAt(i, 0);
            if (cellValue instanceof ImageIcon)
            {
                ImageIcon icon = (ImageIcon) cellValue;
                if (icon.getDescription().equals(itemName))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    private void adjustSecondaryTableColumnWidths()
    {
        if (secondaryTable == null) return;

        TableColumnModel columnModel = secondaryTable.getColumnModel();
        if (columnModel.getColumnCount() == 5)
        {
            columnModel.getColumn(0).setPreferredWidth(50); // Item
            columnModel.getColumn(1).setPreferredWidth(50); // Split
            columnModel.getColumn(2).setPreferredWidth(100); // Date/Kill Count
            columnModel.getColumn(3).setPreferredWidth(40); // Team Size
            columnModel.getColumn(4).setPreferredWidth(40); // Received/Seen
        }
    }

    public String convertShortNameToFullName(String shortName)
    {
        for (NexUniques unique : NexUniques.values())
        {
            if (unique.getShortName().equals(shortName))
            {
                return unique.getFullName();
            }
        }
        return shortName; // Fallback
    }

    private String getShortNameForItem(String fullName)
    {
        for (NexUniques unique : NexUniques.values())
        {
            if (unique.getFullName().equals(fullName))
            {
                return unique.getShortName();
            }
        }
        return fullName; // Fallback if no match found
    }

    public DefaultTableModel getSecondaryTableModel()
    {
        return secondaryTableModel;
    }

    public JTable getSecondaryTable()
    {
        return secondaryTable;
    }

    public DefaultTableModel getPrimaryTableModel()
    {
        return primaryTableModel;
    }

    public JTable getPrimaryTable()
    {
        return primaryTable;
    }

    public static class ItemAggregatedData
    {
        private int receivedCount = 0;
        private int seenCount = 0;
        private double totalSplit = 0.0;

        public void increaseReceived()
        {
            receivedCount++;
        }

        public void increaseSeen()
        {
            seenCount++;
        }

        public void increaseSplit(double split)
        {
            totalSplit += split;
        }

        public int getReceivedCount()
        {
            return receivedCount;
        }

        public int getSeenCount()
        {
            return seenCount;
        }

        public double getTotalSplit()
        {
            return totalSplit;
        }
    }

    private static class ImageRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (value instanceof ImageIcon)
            {
                JLabel label = new JLabel((ImageIcon) value);
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}