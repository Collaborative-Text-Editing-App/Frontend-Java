package org.example;

import org.example.controller.CollaborativeEditorController;
import org.example.service.CollaborativeEditorService;
import org.example.service.WebSocketService;
import org.example.view.CollaborativeEditorPanel;
import org.example.view.HomeScreenPanel;

import java.awt.*;
import java.io.File;
import javax.swing.*;

import static org.example.view.styling.UIStyle.styleButton;

public class Main {

    public static void main(String[] args) {
        // Initialize services
        CollaborativeEditorService collaborationService = new CollaborativeEditorService();
        // Initialize controller with connection parameters
        CollaborativeEditorController controller =
                new CollaborativeEditorController(
                        collaborationService.host,
                        collaborationService.port,
                        null
                );

        // Create and show home screen
        SwingUtilities.invokeLater(() -> {
            HomeScreenPanel homeScreenPanel = new HomeScreenPanel(controller);
            homeScreenPanel.pack();
            homeScreenPanel.setLocationRelativeTo(null);
            homeScreenPanel.setVisible(true);
        });
    }
}