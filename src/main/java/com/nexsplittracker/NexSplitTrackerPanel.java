package com.nexsplittracker;

import com.google.gson.Gson;
import net.runelite.client.RuneLite;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.File;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NexSplitTrackerPanel extends PluginPanel
{
    private final ItemManager itemManager;
    private JTable dropTable;
    private DefaultTableModel tableModel;
    private JTable itemDetailsTable;
    private DefaultTableModel itemDetailsTableModel;
    private JComboBox<String> itemComboBox;
    private CustomTextField splitTextField;
    private JCheckBox receivedCheckBox;
    private final Gson gson;

    private final List<ItemData> itemDataList = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(NexSplitTrackerPanel.class.getName());
    private static final File PLUGIN_DIR = new File(RuneLite.RUNELITE_DIR, "NexSplitTracker");

    public NexSplitTrackerPanel(ItemManager itemManager, Gson gson)
    {
        super();
        this.itemManager = itemManager;
        this.gson = gson;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        createHeader();
        initializeTable();
        initializeTableData();
        userInputPanel();
        initializeSecondaryTable();

        try
        {
            createPluginDirectory();
            String dataFilePath = new File(PLUGIN_DIR, "data.json").getAbsolutePath();
            loadDataFromFile(dataFilePath);
            updateTablesFromLoadedData();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Error initializing plugin: ", e);
        }

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


    private void createPluginDirectory()
    {
        if (!PLUGIN_DIR.exists())
        {
            PLUGIN_DIR.mkdirs(); // Create directory if it doesn't exist
        }
    }


    private void saveDataToFile(String filePath)
    {
        try
        {
            FileWriter writer = new FileWriter(filePath);
            gson.toJson(itemDataList, writer);
            writer.flush();
            writer.close();
            logger.info("Data saved to file: " + filePath);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error saving data to file: ", e);
        }
    }


    private void loadDataFromFile(String filePath)
    {
        try
        {
            Reader reader = new FileReader(filePath);
            Type listType = new TypeToken<ArrayList<ItemData>>(){}.getType();
            List<ItemData> loadedData = gson.fromJson(reader, listType);
            if (loadedData != null)
            {
                itemDataList.addAll(loadedData);
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        updatePrimaryTable();
        logger.info("Data loaded from file: " + filePath);
    }

    private void updateTablesFromLoadedData()
    {
        resetPrimaryTableData();
        Map<String, ItemAggregatedData> aggregatedDataMap = aggregateDataForPrimaryTable();
        updatePrimaryTableWithAggregatedData(aggregatedDataMap);
        updateSecondaryTable();
    }


    private Map<String, ItemAggregatedData> aggregateDataForPrimaryTable()
    {
        Map<String, ItemAggregatedData> aggregatedDataMap = new HashMap<>();
        for (ItemData item : itemDataList)
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
        return aggregatedDataMap;
    }


    private void updatePrimaryTableWithAggregatedData(Map<String, ItemAggregatedData> aggregatedDataMap)
    {
        for (Map.Entry<String, ItemAggregatedData> entry : aggregatedDataMap.entrySet())
        {
            String itemName = entry.getKey();
            ItemAggregatedData aggregatedData = entry.getValue();

            int index = findRowIndexByItemName(itemName);
            if (index != -1)
            {
                int totalOccurrences = getTotalOccurrencesOfItem(itemName);
                int seenCount = totalOccurrences - aggregatedData.getReceivedCount();

                tableModel.setValueAt(aggregatedData.getReceivedCount(), index, 1); // Update 'Received' count
                tableModel.setValueAt(seenCount, index, 2); // Update 'Seen' count
                tableModel.setValueAt(aggregatedData.getTotalSplit(), index, 3); // Update 'Split' value
            }
        }
    }



    private int getTotalOccurrencesOfItem(String itemName)
    {
        int count = 0;
        for (ItemData item : itemDataList)
        {
            if (item.getItemName().equals(itemName))
            {
                count++;
            }
        }
        return count;
    }


    private void initializeTable()
    {
        String[] columnNames = {"Drop", "Received", "Seen", "Split"};
        tableModel = new DefaultTableModel(columnNames, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
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

        tableModel.addRow(new Object[]{"Drop", "Received", "Seen", "Split"});
        tableModel.addRow(new Object[]{"Total", 0, 0, 0});
    }

    private void initializeTableData()
    {
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
        itemComboBox.setToolTipText("Select the item you want to add");

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
        receivedCheckBox.setToolTipText("Check this box if you received the drop in your name");

        // Split amount input
        gbc.gridx = 0;
        gbc.gridy = 2;
        splitTextField = new CustomTextField(10);
        splitTextField.setSuffix("M");
        userInputPanel.add(new JLabel("Split Amount:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(splitTextField, gbc);
        splitTextField.setToolTipText("Enter the split amount in Millions. Example:'50' or '65.5'");

        // Team size input
        gbc.gridx = 0;
        gbc.gridy = 3;
        JTextField teamSizeField = new JTextField(10);
        userInputPanel.add(new JLabel("Team Size:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(teamSizeField, gbc);
        teamSizeField.setToolTipText("Enter in the team size");

        // 'Add Drop' button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Drop");
        addButton.setToolTipText("Click to add a new drop");

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
        String[] detailColumnNames = {"Item", "Split", "Date", "Team", "Drop"};
        itemDetailsTableModel = new DefaultTableModel(detailColumnNames, 0);
        itemDetailsTable = new JTable(itemDetailsTableModel);
        itemDetailsTable.setTableHeader(new ToolTipHeader(itemDetailsTable.getColumnModel()));

        // Center cell text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        // Set the renderer to each column
        for (int i = 0; i < itemDetailsTableModel.getColumnCount(); i++)
        {
            itemDetailsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Listener for delete
        itemDetailsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int row = itemDetailsTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < itemDetailsTable.getRowCount())
                    {
                        itemDetailsTable.setRowSelectionInterval(row, row);
                        showDeletePopup(e.getX(), e.getY(), row);
                    }
                }
            }
        });


        // Configure the table to use the short names in the "Item" column
        itemDetailsTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer()
        {
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
        itemDetailsScrollPane.setPreferredSize(new Dimension(400, 250));

        // Add the JScrollPane to panel
        add(itemDetailsScrollPane, BorderLayout.SOUTH);

        // Schedule the adjustment of column widths
        SwingUtilities.invokeLater(this::adjustColumnWidths);
    }

    private void adjustColumnWidths()
    {
        TableColumnModel columnModel = itemDetailsTable.getColumnModel();
        if (columnModel.getColumnCount() == 5)
        {
            columnModel.getColumn(0).setPreferredWidth(50); // Width for 'Item' column
            columnModel.getColumn(1).setPreferredWidth(50); // Width for 'Split' column
            columnModel.getColumn(2).setPreferredWidth(100); // Width for 'Date' column
            columnModel.getColumn(3).setPreferredWidth(40); // Width for 'Team Size' column
            columnModel.getColumn(4).setPreferredWidth(40); // Width for 'Received or Seen'
        }
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
        for (ItemData item : itemDataList)
        {
            if (item.getItemName().equals(fullName))
            {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null)
        {
            itemDataList.remove(toRemove);
            updatePrimaryTable();
            String dataFilePath = new File(PLUGIN_DIR, "data.json").getAbsolutePath();
            saveDataToFile(dataFilePath);
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

    private void updatePrimaryTable()
    {
        resetPrimaryTableData();

        // Aggregate counts and splits for each unique item in itemDataList
        Map<String, ItemAggregatedData> aggregatedDataMap = new HashMap<>();
        for (ItemData item : itemDataList)
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
                tableModel.setValueAt(aggregatedData.getReceivedCount(), index, 1); // Update 'Received' count
                tableModel.setValueAt(aggregatedData.getSeenCount(), index, 2); // Update 'Seen' count
                tableModel.setValueAt(aggregatedData.getTotalSplit(), index, 3); // Update 'Split' value
            }
        }

        updateTotals();
    }


    public class ItemAggregatedData
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

            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yy"));

            // Add to secondary table
            LocalDate currentDate = LocalDate.now();
            ItemData newItem = new ItemData(itemName, splitAmount, formattedDate, teamSize, isReceived);
            itemDataList.add(newItem);
            updateSecondaryTable();
        }
        updateTotals();
        String dataFilePath = new File(PLUGIN_DIR, "data.json").getAbsolutePath();
        saveDataToFile(dataFilePath);
    }

    private int findRowIndexByItemName(String itemName)
    {
        for (int i = 0; i < tableModel.getRowCount(); i++)
        {
            Object cellValue = tableModel.getValueAt(i, 0);
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

    private void updateSecondaryTable()
    {
        itemDetailsTableModel.setRowCount(0); // Clear the table
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        for (ItemData item : itemDataList)
        {
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> removeItem(item));

            LocalDate date = LocalDate.parse(item.getDate(), formatter);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd/yy"));
            String drop = item.isReceived() ? "R" : "S";

            itemDetailsTableModel.addRow(new Object[]{
                    item.getItemName(),
                    item.getSplitAmount(),
                    formattedDate,
                    item.getTeamSize(),
                    drop,
                    removeButton
            });
        }
    }


    private void removeItem(ItemData item)
    {
        itemDataList.remove(item);
        updateSecondaryTable();
        String dataFilePath = new File(PLUGIN_DIR, "data.json").getAbsolutePath();
        saveDataToFile(dataFilePath);
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

