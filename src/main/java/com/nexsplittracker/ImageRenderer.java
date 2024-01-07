package com.nexsplittracker; // Adjust the package name as per your project structure

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ImageRenderer extends DefaultTableCellRenderer
{
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column)
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
