package com.nexsplittracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditEntryDialog extends JDialog
{
    private final ItemDataV2 originalItem;
    private final NexSplitTrackerConfig config;
    private final ItemDataV2 editedItem;

    private JComboBox<String> itemComboBox;
    private CustomTextField splitTextField;
    private JTextField teamSizeField;
    private JTextField dateField;
    private JTextField killCountField;
    private JCheckBox receivedCheckBox;

    private boolean confirmed = false;

    public EditEntryDialog(Frame parent, ItemDataV2 item, NexSplitTrackerConfig config)
    {
        super(parent, "Edit Drop Entry", true);
        this.originalItem = item;
        this.config = config;
        this.editedItem = new ItemDataV2(
            item.getItemName(),
            item.getSplitAmount(),
            item.getDate(),
            item.getTeamSize(),
            item.isReceived(),
            item.getKillCount()
        );

        initializeDialog();
        populateFields();
    }

    private void initializeDialog()
    {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Item selection
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1;
        itemComboBox = new JComboBox<>(NexUniques.names());
        itemComboBox.setPreferredSize(new Dimension(200, 25));
        mainPanel.add(itemComboBox, gbc);

        // Split amount
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Split Amount (M):"), gbc);
        gbc.gridx = 1;
        splitTextField = new CustomTextField(15);
        splitTextField.setSuffix("M");
        mainPanel.add(splitTextField, gbc);

        // Team size
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Team Size:"), gbc);
        gbc.gridx = 1;
        teamSizeField = new JTextField(15);
        mainPanel.add(teamSizeField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Date (MM/dd/yy):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setToolTipText("Format: MM/dd/yy (e.g., 12/25/23)");
        mainPanel.add(dateField, gbc);

        // Kill count (if enabled)
        if (config.enableKillCountEntry())
        {
            gbc.gridx = 0; gbc.gridy = 4;
            mainPanel.add(new JLabel("Kill Count:"), gbc);
            gbc.gridx = 1;
            killCountField = new JTextField(15);
            killCountField.setToolTipText("Leave empty if unknown");
            mainPanel.add(killCountField, gbc);
        }

        // Received checkbox
        gbc.gridx = 0; gbc.gridy = config.enableKillCountEntry() ? 5 : 4;
        mainPanel.add(new JLabel("Received by Me:"), gbc);
        gbc.gridx = 1;
        receivedCheckBox = new JCheckBox();
        mainPanel.add(receivedCheckBox, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(new SaveActionListener());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Dialog properties
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private void populateFields()
    {
        // Set item selection
        itemComboBox.setSelectedItem(originalItem.getItemName());

        // Set split amount
        splitTextField.setText(String.valueOf(originalItem.getSplitAmount()));

        // Set team size
        teamSizeField.setText(String.valueOf(originalItem.getTeamSize()));

        // Set date
        dateField.setText(originalItem.getDate());

        // Set kill count (if field exists and item has kill count)
        if (killCountField != null)
        {
            if (originalItem.hasKillCount())
            {
                killCountField.setText(originalItem.getKillCount().toString());
            }
            else
            {
                killCountField.setText("");
            }
        }

        // Set received status
        receivedCheckBox.setSelected(originalItem.isReceived());
    }

    private class SaveActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                // Validate and update item data
                String selectedItem = (String) itemComboBox.getSelectedItem();
                double splitAmount = validateAndParseDouble(splitTextField.getText(), "Split Amount");
                int teamSize = validateAndParseInt(teamSizeField.getText(), "Team Size");
                String date = validateDate(dateField.getText());
                boolean isReceived = receivedCheckBox.isSelected();

                Integer killCount = null;
                if (killCountField != null && !killCountField.getText().trim().isEmpty())
                {
                    killCount = validateAndParseInt(killCountField.getText(), "Kill Count");
                }

                // Update the edited item
                editedItem.setItemName(selectedItem);
                editedItem.setSplitAmount(splitAmount);
                editedItem.setTeamSize(teamSize);
                editedItem.setDate(date);
                editedItem.setReceived(isReceived);
                editedItem.setKillCount(killCount);

                confirmed = true;
                dispose();

            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(
                    EditEntryDialog.this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
            }
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
            double value = Double.parseDouble(text.trim());
            if (value < 0)
            {
                throw new NumberFormatException(fieldName + " must be a positive number");
            }
            return value;
        }
        catch (NumberFormatException e)
        {
            throw new NumberFormatException("Invalid " + fieldName + ": Please enter a valid positive number");
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

    private String validateDate(String text) throws IllegalArgumentException
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new IllegalArgumentException("Date cannot be empty");
        }

        try
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            LocalDate.parse(text.trim(), formatter);
            return text.trim();
        }
        catch (DateTimeParseException e)
        {
            throw new IllegalArgumentException("Invalid date format. Please use MM/dd/yy format (e.g., 12/25/23)");
        }
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public ItemDataV2 getEditedItem()
    {
        return editedItem;
    }
}