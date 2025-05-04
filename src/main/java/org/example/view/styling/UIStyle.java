package org.example.view.styling;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UIStyle {

    public static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 16);

    public static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    public static final Color FOREGROUND_COLOR = Color.WHITE;

    public static void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(FOREGROUND_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    public static void styleTextField(JTextField field) {
        Border line = BorderFactory.createLineBorder(PRIMARY_COLOR);
        Border margin = BorderFactory.createEmptyBorder(6, 8, 6, 8);
        field.setBorder(BorderFactory.createCompoundBorder(line, margin));
        field.setFont(TEXT_FONT);
    }
}
