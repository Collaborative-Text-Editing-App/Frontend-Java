package org.example;

import org.example.service.WebSocketService;
import org.example.view.CollaborativeEditorPanel;

import java.awt.*;
import java.io.File;
import javax.swing.*;

import static org.example.view.styling.UIStyle.styleButton;

public class Main {
    private static WebSocketService webSocketService;

    public static void main(String[] args) {
        // Initialize WebSocket service
        webSocketService = new WebSocketService();
        webSocketService.connect();

        // Create and set up the main frame
        JFrame frame = new JFrame("Collaborative Text Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                webSocketService.disconnect();
            }
        });
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null); // Center the window
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title label at the top
        JLabel titleLabel = new JLabel("Collaborative Text Editor", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create button panel for options
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Create New Document button
        JButton newDocButton = new JButton("Create New Document");
        styleButton(newDocButton);
        newDocButton.addActionListener(e -> {
            JFrame editorFrame = new JFrame("Collaborative Editor");
            editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            editorFrame.setSize(800, 500);

            // Add the custom panel with WebSocket service
            CollaborativeEditorPanel editorPanel = new CollaborativeEditorPanel(webSocketService);
            editorFrame.getContentPane().add(editorPanel);

            // Show new window
            editorFrame.setVisible(true);
        });

        // Create Import Document button
        JButton importButton = new JButton("Import .txt Document");
        styleButton(importButton);
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // Create editor window and load file
                JFrame editorFrame = new JFrame("Collaborative Editor - " + selectedFile.getName());
                editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editorFrame.setSize(800, 500);

                CollaborativeEditorPanel editorPanel = new CollaborativeEditorPanel(webSocketService);
                editorFrame.getContentPane().add(editorPanel);
                editorPanel.loadFile(selectedFile);

                editorFrame.setVisible(true);
            }
        });

        // Create Join Session button
        JButton joinButton = new JButton("Join Session by Code");
        styleButton(joinButton);
        joinButton.addActionListener(e -> {
            String sessionCode = JOptionPane.showInputDialog(frame, "Enter session code:", "Join Session", JOptionPane.PLAIN_MESSAGE);
            if (sessionCode != null && !sessionCode.trim().isEmpty()) {
                // Send join request to WebSocket server
                webSocketService.sendMessage("/app/join", sessionCode);
                JOptionPane.showMessageDialog(frame, "Joining session with code: " + sessionCode);
            }
        });

        // Add buttons to panel
        buttonPanel.add(newDocButton);
        buttonPanel.add(importButton);
        buttonPanel.add(joinButton);

        // Add button panel to main panel
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add some padding and set content
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}