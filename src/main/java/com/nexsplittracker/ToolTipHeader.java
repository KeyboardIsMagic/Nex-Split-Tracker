package com.nexsplittracker;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseEvent;

class ToolTipHeader extends JTableHeader
{
    public ToolTipHeader(TableColumnModel model)
    {
        super(model);
    }

    @Override
    public String getToolTipText(MouseEvent e)
    {
        int index = columnAtPoint(e.getPoint());
        int realIndex = getTable().convertColumnIndexToModel(index);

        // Get the column name to determine what tooltip to show
        String columnName = getTable().getColumnName(realIndex);

        switch (realIndex)
        {
            case 0: return "Item Name";
            case 1: return "Split amount in millions";
            case 2:
                if ("Kill Count".equals(columnName)) {
                    return "Kill count when this drop occurred";
                } else {
                    return "Date the drop was added to the tracker";
                }
            case 3: return "Team Size";
            case 4: return "<html>R: Received<br>S: Seen</html>";
            default: return "";
        }
    }
}
