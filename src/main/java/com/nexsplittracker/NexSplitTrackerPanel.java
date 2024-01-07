package com.nexsplittracker;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;

public class NexSplitTrackerPanel extends PluginPanel {
    private final ItemManager itemManager;
    private JTable dropTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> itemComboBox;
    private JTextField splitTextField;
    private JCheckBox receivedCheckBox;

    public NexSplitTrackerPanel(ItemManager itemManager) {
        super();
        this.itemManager = itemManager;
        setLayout(new BorderLayout());

        createHeader();
        initializeTable();
        initializeTableData();
        createUserInputPanel();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.DARK_GRAY);

        JLabel headerLabel = new JLabel("Nex Split Tracker", SwingConstants.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
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
        tableModel.addRow(new Object[]{"Total", 0, 0, 0.0}); // Footer row for totals
    }

    private void initializeTableData() {
        for (NexUniques unique : NexUniques.values()) {
            ImageIcon icon = new ImageIcon(itemManager.getImage(unique.getItemId()));
            icon.setDescription(unique.getName());
            Object[] row = {icon, 0, 0, 0.0};
            tableModel.insertRow(tableModel.getRowCount() - 1, row);
        }
    }

    private void createUserInputPanel() {
        JPanel userInputPanel = new JPanel();
        userInputPanel.setLayout(new GridLayout(4, 2, 5, 5));
        userInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] itemNames = Arrays.stream(NexUniques.values())
                .map(NexUniques::getName)
                .toArray(String[]::new);
        itemComboBox = new JComboBox<>(itemNames);
        receivedCheckBox = new JCheckBox("Received by Me");
        splitTextField = new JTextField();

        JButton addButton = new JButton("Add Drop");
        addButton.addActionListener(e -> {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            boolean isReceived = receivedCheckBox.isSelected();
            double splitAmount;
            try {
                splitAmount = Double.parseDouble(splitTextField.getText());
            } catch (NumberFormatException ex) {
                splitAmount = 0.0; // Handle invalid number format
            }
            updateItem(selectedItem, isReceived, splitAmount);
            updateTotals();
        });

        userInputPanel.add(new JLabel("Select Item:"));
        userInputPanel.add(itemComboBox);
        userInputPanel.add(new JLabel("Received by Me:"));
        userInputPanel.add(receivedCheckBox);
        userInputPanel.add(new JLabel("Split Amount:"));
        userInputPanel.add(splitTextField);
        userInputPanel.add(addButton);

        add(userInputPanel, BorderLayout.SOUTH);
    }

    private void updateItem(String itemName, boolean received, double splitAmount) {
        int index = findRowIndexByItemName(itemName);
        if (index != -1 && index < tableModel.getRowCount() - 1) {
            int currentReceived = (Integer) tableModel.getValueAt(index, 1);
            int currentSeen = (Integer) tableModel.getValueAt(index, 2);
            double currentSplit = (Double) tableModel.getValueAt(index, 3);

            if (received) {
                currentReceived++;
            } else {
                currentSeen++;
            }

            currentSplit += splitAmount;

            tableModel.setValueAt(currentReceived, index, 1);
            tableModel.setValueAt(currentSeen, index, 2);
            tableModel.setValueAt(currentSplit, index, 3);
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

    private void updateTotals() {
        int totalDrops = 0;
        int totalReceived = 0;
        int totalSeen = 0;
        double totalSplit = 0.0;

        for (int i = 1; i < tableModel.getRowCount() - 1; i++) {
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

    private class ImageRenderer extends DefaultTableCellRenderer {
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
