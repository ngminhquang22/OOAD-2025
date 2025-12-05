package com.gaminglounge;

import java.awt.Color;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.gaminglounge.gui.LoginWindow;
import com.gaminglounge.utils.DatabaseHelper;

public class Main {
    public static void main(String[] args) {
        // Setup FlatLaf with custom properties for a more modern look
        try {
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            
            // Custom Accent Color (Gaming Blue)
            UIManager.put("Component.accentColor", new Color(0, 122, 204));
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("TabbedPane.showTabSeparators", true);
            
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Initialize Database
        DatabaseHelper.initDB();

        // Run GUI
        SwingUtilities.invokeLater(() -> {
            new LoginWindow().setVisible(true);
        });
    }
}
