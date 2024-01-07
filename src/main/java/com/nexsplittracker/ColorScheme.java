package com.nexsplittracker;

import java.awt.Color;

public class ColorScheme {
    public static final Color DARK_GRAY_COLOR = new Color(42, 42, 42);
    public static final Color MEDIUM_GRAY_COLOR = new Color(77, 77, 77);
    public static final Color LIGHT_GRAY_COLOR = new Color(107, 107, 107);
    public static final Color BRAND_ORANGE = new Color(217, 83, 25);
    public static final Color BRAND_BLUE = new Color(0, 123, 255);
    // ... Add more colors as needed

    // Private constructor to prevent instantiation
    private ColorScheme()
    {
        throw new IllegalStateException("Utility class");
    }
}
