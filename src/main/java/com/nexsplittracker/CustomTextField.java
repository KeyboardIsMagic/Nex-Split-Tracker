package com.nexsplittracker;

import javax.swing.*;
import java.awt.*;

public class CustomTextField extends JTextField
{
    private String suffix = "";
    private Color suffixColor = new Color(150, 150, 150); // Light gray

    public CustomTextField(int columns) {super(columns);}

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        int suffixWidth = g.getFontMetrics().stringWidth(suffix);
        int textX = getWidth() - suffixWidth - 5; // 5 pixels padding from the right
        int textY = getBaseline(getWidth(), getHeight());

        g.setColor(suffixColor);
        g.drawString(suffix, textX, textY);
    }

    public void setSuffix(String suffix)
    {this.suffix = suffix;}

}
