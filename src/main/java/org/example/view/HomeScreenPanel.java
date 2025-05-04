package org.example.view;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.example.service.WebSocketService;
import org.example.controller.CollaborativeEditorController;

public class HomeScreenPanel extends JFrame {
    private final CollaborativeEditorController controller;


    public HomeScreenPanel(CollaborativeEditorController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Collaborative Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Collaborative Text Editor", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton newDocButton = createStyledButton("Create New Document");
        newDocButton.addActionListener(e -> controller.createNewDocument());

        JButton importButton = createStyledButton("Import .txt Document");
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = fileChooser.getSelectedFile();
                if (selected != null && selected.getName().toLowerCase().endsWith(".txt")) {
                    try {
                        String content = Files.readString(selected.toPath());
                        // TODO: open editor panel with imported content
                        controller.openImportedDocument(selected.getName(), content);
                    } catch (IOException ioEx) {
                        showError("Failed to read file: " + ioEx.getMessage());
                    }
                } else {
                    showError("Please select a valid .txt file.");
                }
            }
        });

        JButton joinButton = createStyledButton("Join Session by Code");
        joinButton.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(
                    this,
                    "Enter session code:",
                    "Join Session",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (code != null && !code.trim().isEmpty()) {
                try {
                    controller.joinDoc(code.trim());
                } catch (Exception ex) {
                    showError("Error joining session: " + ex.getMessage());
                }
            }
        });

        buttonPanel.add(newDocButton);
        buttonPanel.add(importButton);
        buttonPanel.add(joinButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(200, 60));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}