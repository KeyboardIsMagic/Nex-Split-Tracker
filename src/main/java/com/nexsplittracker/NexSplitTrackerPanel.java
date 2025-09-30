package com.nexsplittracker;

import com.google.gson.Gson;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class NexSplitTrackerPanel extends PluginPanel
{
    private final ItemManager itemManager;
    private final NexSplitTrackerConfig config;
    private JComboBox<String> itemComboBox;
    private CustomTextField splitTextField;
    private JTextField killCountField;
    private JLabel killCountLabel;
    private JCheckBox receivedCheckBox;
    private JTextField teamSizeField;
    private JPanel userInputPanel;

    private final DataManager dataManager;
    private final TableManager tableManager;
    private static final Logger logger = Logger.getLogger(NexSplitTrackerPanel.class.getName());

    public NexSplitTrackerPanel(ItemManager itemManager, Gson gson, NexSplitTrackerConfig config)
    {
        super();
        this.itemManager = itemManager;
        this.config = config;
        this.dataManager = new DataManager(gson);
        this.tableManager = new TableManager(itemManager, config);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initializeUI();
        loadData();
    }


    private void initializeUI()
    {
        createHeader();
        add(tableManager.initializePrimaryTable(), BorderLayout.CENTER);
        createUserInputPanel();

        JScrollPane secondaryScrollPane = new JScrollPane(tableManager.initializeSecondaryTable());
        secondaryScrollPane.setPreferredSize(new Dimension(400, 250));
        add(secondaryScrollPane, BorderLayout.SOUTH);

        setupSecondaryTableMouseListener();
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

    private void loadData()
    {
        dataManager.loadData();
        updateTables();
    }

    private void updateTables()
    {
        tableManager.updatePrimaryTable(dataManager.getAllItems());
        tableManager.updateSecondaryTable(dataManager.getAllItems());
    }

    public void onConfigChanged()
    {
        // Update the secondary table to reflect any config changes (show kc/date)
        updateTables();

        // Update kill count field visibility
        updateKillCountFieldVisibility();
    }

    private void updateKillCountFieldVisibility()
    {
        if (killCountField != null && killCountLabel != null)
        {
            boolean shouldShow = config.enableKillCountEntry();
            killCountField.setVisible(shouldShow);
            killCountLabel.setVisible(shouldShow);

            // Refresh the layout
            if (userInputPanel != null)
            {
                userInputPanel.revalidate();
                userInputPanel.repaint();
            }
        }
    }

    private void addNewDrop()
    {
        try
        {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            boolean isReceived = receivedCheckBox.isSelected();
            double splitAmount = validateAndParseDouble(splitTextField.getText(), "Split Amount");
            int teamSize = validateAndParseInt(teamSizeField.getText(), "Team Size");

            Integer killCount = null;
            if (config.enableKillCountEntry() && killCountField != null &&
                killCountField.isVisible() && !killCountField.getText().trim().isEmpty())
            {
                killCount = validateAndParseInt(killCountField.getText(), "Kill Count");
            }

            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yy"));
            ItemDataV2 newItem = new ItemDataV2(selectedItem, splitAmount, formattedDate, teamSize, isReceived, killCount);

            dataManager.addItem(newItem);
            updateTables();
            clearInputFields();

        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double validateAndParseDouble(String text, String fieldName) throws NumberFormatException
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new NumberFormatException(fieldName + " cannot be empty");
        }
        try
        {
            return Double.parseDouble(text.trim());
        }
        catch (NumberFormatException e)
        {
            throw new NumberFormatException("Invalid " + fieldName + ": Please enter a valid number");
        }
    }

    private int validateAndParseInt(String text, String fieldName) throws NumberFormatException
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new NumberFormatException(fieldName + " cannot be empty");
        }
        try
        {
            int value = Integer.parseInt(text.trim());
            if (value < 0)
            {
                throw new NumberFormatException(fieldName + " must be a positive number");
            }
            return value;
        }
        catch (NumberFormatException e)
        {
            throw new NumberFormatException("Invalid " + fieldName + ": Please enter a valid positive integer");
        }
    }

    private void clearInputFields()
    {
        splitTextField.setText("");
        teamSizeField.setText("");
        if (killCountField != null)
        {
            killCountField.setText("");
        }
        receivedCheckBox.setSelected(false);
    }

    private void setupSecondaryTableMouseListener()
    {
        tableManager.getSecondaryTable().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                int row = tableManager.getSecondaryTable().rowAtPoint(e.getPoint());
                if (row >= 0 && row < tableManager.getSecondaryTable().getRowCount())
                {
                    tableManager.getSecondaryTable().setRowSelectionInterval(row, row);

                    if (SwingUtilities.isRightMouseButton(e))
                    {
                        showDeletePopup(e.getX(), e.getY(), row);
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
                {
                    int row = tableManager.getSecondaryTable().rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tableManager.getSecondaryTable().getRowCount())
                    {
                        editTableEntry(row);
                    }
                }
            }
        });
    }

    private void createUserInputPanel()
    {
        userInputPanel = new JPanel(new GridBagLayout());
        userInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Item selection
        String[] itemNames = NexUniques.names();
        itemComboBox = new JComboBox<>(itemNames);
        userInputPanel.add(new JLabel("Select Item:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(itemComboBox, gbc);
        itemComboBox.setToolTipText("Select the item you want to add");

        // Received checkbox
        gbc.gridx = 0;
        gbc.gridy = 1;
        userInputPanel.add(new JLabel("Received by Me:"), gbc);
        gbc.gridx = 1;
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
        teamSizeField = new JTextField(10);
        userInputPanel.add(new JLabel("Team Size:"), gbc);
        gbc.gridx = 1;
        userInputPanel.add(teamSizeField, gbc);
        teamSizeField.setToolTipText("Enter the team size");

        // Kill count input (always create but conditionally show)
        gbc.gridx = 0;
        gbc.gridy = 4;
        killCountLabel = new JLabel("Kill Count:");
        killCountField = new JTextField(10);
        killCountField.setToolTipText("Enter the kill count when this drop occurred (optional)");

        // Add kill count components to panel
        userInputPanel.add(killCountLabel, gbc);
        gbc.gridx = 1;
        userInputPanel.add(killCountField, gbc);

        // Set initial visibility based on config
        updateKillCountFieldVisibility();

        // Add Drop button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Drop");
        addButton.setToolTipText("Click to add a new drop");
        addButton.addActionListener(e -> addNewDrop());
        userInputPanel.add(addButton, gbc);

        add(userInputPanel, BorderLayout.SOUTH);
    }

    private void showDeletePopup(int x, int y, int row)
    {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> editTableEntry(row));
        popupMenu.add(editItem);

        popupMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this entry?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                deleteItemFromTable(row);
            }
        });
        popupMenu.add(deleteItem);
        popupMenu.show(tableManager.getSecondaryTable(), x, y);
    }

    private void editTableEntry(int row)
    {
        try {
            // Get the item details from the table
            String displayName = (String) tableManager.getSecondaryTableModel().getValueAt(row, 0);
            double splitAmount = (Double) tableManager.getSecondaryTableModel().getValueAt(row, 1);
            String dateOrKillCount = (String) tableManager.getSecondaryTableModel().getValueAt(row, 2);
            int teamSize = (Integer) tableManager.getSecondaryTableModel().getValueAt(row, 3);

            // Find the actual item in the data
            ItemDataV2 itemToEdit = dataManager.findItemByTableRowData(displayName, splitAmount, dateOrKillCount, teamSize, config);

            if (itemToEdit != null) {
                // Create and show edit dialog
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                EditEntryDialog editDialog = new EditEntryDialog(parentFrame, itemToEdit, config);
                editDialog.setVisible(true);

                // If user confirmed the edit, update the data
                if (editDialog.isConfirmed()) {
                    ItemDataV2 editedItem = editDialog.getEditedItem();
                    dataManager.updateItem(itemToEdit, editedItem);
                    updateTables();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not find the selected item to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            logger.severe("Error editing item: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error editing item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItemFromTable(int row)
    {
        try {
            // Get the item details from the table
            String itemName = (String) tableManager.getSecondaryTableModel().getValueAt(row, 0);
            double splitAmount = (Double) tableManager.getSecondaryTableModel().getValueAt(row, 1);
            String dateOrKillCount = (String) tableManager.getSecondaryTableModel().getValueAt(row, 2);
            int teamSize = (Integer) tableManager.getSecondaryTableModel().getValueAt(row, 3);

            // Convert short name back to full name for matching
            String fullName = tableManager.convertShortNameToFullName(itemName);

            // Find the matching item in the data
            ItemDataV2 toRemove = null;
            for (ItemDataV2 item : dataManager.getAllItems()) {
                if (item.getItemName().equals(fullName) &&
                    item.getSplitAmount() == splitAmount &&
                    item.getTeamSize() == teamSize) {

                    // Additional check for date match
                    if (!config.showKillCount() && item.getDate().equals(dateOrKillCount)) {
                        toRemove = item;
                        break;
                    }
                    // Additional check for kill count match
                    else if (config.showKillCount() && item.hasKillCount() &&
                             item.getKillCount().toString().equals(dateOrKillCount)) {
                        toRemove = item;
                        break;
                    }
                    // If no kill count or N/A displayed
                    else if (config.showKillCount() && !item.hasKillCount() && "N/A".equals(dateOrKillCount)) {
                        toRemove = item;
                        break;
                    }
                }
            }

            if (toRemove != null) {
                dataManager.removeItem(toRemove);
                updateTables();
            }
        } catch (Exception e) {
            logger.severe("Error deleting item: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}