package com.nexsplittracker;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NexSplitTrackerPanel extends PluginPanel {
    private final ItemManager itemManager;
    private JTable dropTable;
    private DefaultTableModel tableModel;
    private JTable itemDetailsTable;
    private DefaultTableModel itemDetailsTableModel;
    private JComboBox<String> itemComboBox;
    private JTextField splitTextField;
    private JCheckBox receivedCheckBox;

    private final List<ItemData> itemDataList = new ArrayList<>();

    public NexSplitTrackerPanel(ItemManager itemManager) {
        super();
        this.itemManager = itemManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createHeader();
        initializeTable();
        initializeTableData();
        userInputPanel();
        initializeSecondaryTable();
    }

    private void createHeader()
    {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel headerLabel = new JLabel("Nex Split Tracker", SwingConstants.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel);
    }

    private void initializeTable() {
        String[] columnNames = {"Drop", "Received", "Seen", "Split"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all rows non-editable
            }
        };

        dropTable = new JTable(tableModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        dropTable.setFillsViewportHeight(true);
        dropTable.setDefaultEditor(Object.class, null);
        dropTable.setRowHeight(30);
        dropTable.setShowGrid(true);
        dropTable.setGridColor(Color.BLACK);
        dropTable.setShowHorizontalLines(true);
        dropTable.setShowVerticalLines(false);

        dropTable.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        dropTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        dropTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        dropTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        add(dropTable, BorderLayout.CENTER);

        tableModel.addRow(new Object[]{"Drop", "Received", "Seen", "Split"}); // Header row
        tableModel.addRow(new Object[]{"Total", 0, 0, 0}); // Footer row for totals
    }

    private void initializeTableData() {
        for (NexUniques unique : NexUniques.values())
        {
            ImageIcon icon = new ImageIcon(itemManager.getImage(unique.getItemId()));
            icon.setDescription(unique.getFullName());
            Object[] row = {icon, 0, 0, 0.0};
            tableModel.insertRow(tableModel.getRowCount() - 1, row);
        }
    }

    private void userInputPanel()
    {
        // Main user input panel with GridBagLayout
        JPanel userInputPanel = new JPanel(new GridBagLayout());
        userInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Adjust component to fill the space
        gbc.weightx = 0.5; // Proportion of extra horizontal space the component should occupy
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Item selection components
        String[] itemNames = NexUniques.names();
        itemComboBox = new JComboBox<>(itemNames);
        userInputPanel.add(new JLabel("Select Item:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(itemComboBox, gbc);

        // 'Received by Me' checkbox
        gbc.anchor = GridBagConstraints.WEST;
        // Label "Received by Me:"
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        gbc.gridwidth = 2; // Takes up 2 column
        userInputPanel.add(new JLabel("Received by Me:"), gbc);
        // Checkbox
        gbc.gridwidth = 1;
        gbc.gridx = 1; // Column 1, next to the label
        gbc.ipadx = 60; // Additional padding in the x-direction
        receivedCheckBox = new JCheckBox();
        userInputPanel.add(receivedCheckBox, gbc);

        // Split amount input
        gbc.gridx = 0;
        gbc.gridy = 2;
        splitTextField = new JTextField(10); // Adjust the size as needed
        userInputPanel.add(new JLabel("Split Amount:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(splitTextField, gbc);

        // Team size input
        gbc.gridx = 0;
        gbc.gridy = 3;
        JTextField teamSizeField = new JTextField(10); // Adjust the size as needed
        userInputPanel.add(new JLabel("Team Size:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(teamSizeField, gbc);

        // 'Add Drop' button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Drop");

        addButton.addActionListener(e ->
        {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            boolean isReceived = receivedCheckBox.isSelected();
            double splitAmount;
            int teamSize;

            try
            {
                splitAmount = Double.parseDouble(splitTextField.getText());
                teamSize = Integer.parseInt(teamSizeField.getText());
            } catch (NumberFormatException ex)
            {
                splitAmount = 0.0; // Handle invalid number format
                teamSize = 0;
            }

            updateItem(selectedItem, isReceived, splitAmount, teamSize, isReceived);
            updateTotals();
        });
        userInputPanel.add(addButton, gbc);

        add(userInputPanel, BorderLayout.SOUTH);
    }

    private void initializeSecondaryTable()
    {
        String[] detailColumnNames = {"Item", "Split", "Date", "Team Size"};
        itemDetailsTableModel = new DefaultTableModel(detailColumnNames, 0);
        itemDetailsTable = new JTable(itemDetailsTableModel);

        // Listener for delete functionality
        itemDetailsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = itemDetailsTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < itemDetailsTable.getRowCount()) {
                        itemDetailsTable.setRowSelectionInterval(row, row);
                        showDeletePopup(e.getX(), e.getY(), row);
                    }
                }
            }
        });


        // Configure the table to use the short names in the "Item" column
        itemDetailsTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                if (value instanceof String)
                {
                    for (NexUniques unique : NexUniques.values())
                    {
                        if (unique.getFullName().equals(value.toString()))
                        {
                            return new JLabel(unique.getShortName());
                        }
                    }
                }
                return new JLabel(value.toString());
            }



        });

        // Add a JScrollPane for the secondary table
        JScrollPane itemDetailsScrollPane = new JScrollPane(itemDetailsTable);
        itemDetailsScrollPane.setPreferredSize(new Dimension(400, 250)); // Set preferred size as needed

        // Add the JScrollPane to panel
        add(itemDetailsScrollPane, BorderLayout.SOUTH);
    }


    private void showDeletePopup(int x, int y, int row)
    {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteItemFromTable(row));
        popupMenu.add(deleteItem);
        popupMenu.show(itemDetailsTable, x, y);
    }

    private void deleteItemFromTable(int row)
    {
        // Get the item name from the secondary table
        String itemName = (String) itemDetailsTableModel.getValueAt(row, 0);

        // Convert short name back to full name for matching
        String fullName = convertShortNameToFullName(itemName);

        // Remove from secondary table
        itemDetailsTableModel.removeRow(row);

        // Find and remove the corresponding item from the itemDataList
        ItemData toRemove = null;
        for (ItemData item : itemDataList) {
            if (item.getItemName().equals(fullName)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            itemDataList.remove(toRemove);
            updatePrimaryTable();
        }
    }

    private String convertShortNameToFullName(String shortName)
    {
        for (NexUniques unique : NexUniques.values())
        {
            if (unique.getShortName().equals(shortName))
            {
                return unique.getFullName();
            }
        }
        return shortName; // Fallback, in case no match is found
    }

    private void updatePrimaryTable() {
        // Reset the primary table's data
        resetPrimaryTableData();

        // Aggregate counts and splits for each unique item in itemDataList
        Map<String, ItemAggregatedData> aggregatedDataMap = new HashMap<>();
        for (ItemData item : itemDataList) {
            String itemName = item.getItemName();
            aggregatedDataMap.putIfAbsent(itemName, new ItemAggregatedData());
            ItemAggregatedData aggregatedData = aggregatedDataMap.get(itemName);

            aggregatedData.increaseReceived(item.isReceived());
            aggregatedData.increaseSplit(item.getSplitAmount());
        }

        // Update the primary table with aggregated data
        for (Map.Entry<String, ItemAggregatedData> entry : aggregatedDataMap.entrySet()) {
            String itemName = entry.getKey();
            ItemAggregatedData aggregatedData = entry.getValue();

            int index = findRowIndexByItemName(itemName);
            if (index != -1) {
                tableModel.setValueAt(aggregatedData.getReceivedCount(), index, 1); // Update 'Received' count
                tableModel.setValueAt(aggregatedData.getSeenCount(), index, 2); // Update 'Seen' count
                tableModel.setValueAt(aggregatedData.getTotalSplit(), index, 3); // Update 'Split' value
            }
        }

        updateTotals();
    }

    private class ItemAggregatedData
    {
        private int receivedCount = 0;
        private double totalSplit = 0.0;

        public void increaseReceived(boolean isReceived)
        {
            if (isReceived)
            {
                receivedCount++;
            }
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
            return itemDataList.size() - receivedCount;
        }

        public double getTotalSplit()
        {
            return totalSplit;
        }
    }

    private void resetPrimaryTableData()
    {
        // Reset the counts and splits for all items in the primary table
        for (int i = 1; i < tableModel.getRowCount() - 1; i++)
        {
            tableModel.setValueAt(0, i, 1); // Reset 'Received' count
            tableModel.setValueAt(0, i, 2); // Reset 'Seen' count
            tableModel.setValueAt(0.0, i, 3); // Reset 'Split' value
        }
    }

    private void updateItem(String itemName, boolean received, double splitAmount, int teamSize, boolean isReceived)
    {
        int index = findRowIndexByItemName(itemName);
        if (index != -1 && index < tableModel.getRowCount() - 1)
        {
            int currentReceived = (Integer) tableModel.getValueAt(index, 1);
            int currentSeen = (Integer) tableModel.getValueAt(index, 2);
            double currentSplit = (Double) tableModel.getValueAt(index, 3);

            if (received)
            {
                currentReceived++;
            }
            else
            {
                currentSeen++;
            }

            currentSplit += splitAmount;

            tableModel.setValueAt(currentReceived, index, 1);
            tableModel.setValueAt(currentSeen, index, 2);
            tableModel.setValueAt(currentSplit, index, 3);

            // Add to secondary table
            LocalDate currentDate = LocalDate.now();
            ItemData newItem = new ItemData(itemName, splitAmount, currentDate, teamSize, isReceived);
            itemDataList.add(newItem);
            updateSecondaryTable();
        }
        updateTotals();
    }

    private int findRowIndexByItemName(String itemName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object cellValue = tableModel.getValueAt(i, 0);
            if (cellValue instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) cellValue;
                if (icon.getDescription().equals(itemName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateSecondaryTable() {
        itemDetailsTableModel.setRowCount(0); // Clear the table
        for (ItemData item : itemDataList) {
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeItem(item));
            itemDetailsTableModel.addRow(new Object[]{
                    item.getItemName(),
                    item.getSplitAmount(),
                    item.getDate(),
                    item.getTeamSize(),
                    removeButton
            });
        }
    }

    private void removeItem(ItemData item)
    {
        itemDataList.remove(item);
        updateSecondaryTable();
    }


    private void updateTotals()
    {
        int totalDrops = 0;
        int totalReceived = 0;
        int totalSeen = 0;
        double totalSplit = 0.0;

        for (int i = 1; i < tableModel.getRowCount() - 1; i++)
        {
            totalReceived += (Integer) tableModel.getValueAt(i, 1);
            totalSeen += (Integer) tableModel.getValueAt(i, 2);
            totalSplit += (Double) tableModel.getValueAt(i, 3);
        }

        totalDrops = totalReceived + totalSeen;

        int lastRowIndex = tableModel.getRowCount() - 1;
        tableModel.setValueAt("Total Drops", lastRowIndex, 0);
        tableModel.setValueAt(totalReceived, lastRowIndex, 1);
        tableModel.setValueAt(totalSeen, lastRowIndex, 2);
        tableModel.setValueAt(totalSplit, lastRowIndex, 3);
    }


    private static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                JLabel label = new JLabel((ImageIcon) value);
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }





}





