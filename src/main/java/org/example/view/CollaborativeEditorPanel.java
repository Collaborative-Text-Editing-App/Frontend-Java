package org.example.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import static org.example.view.styling.UIStyle.styleButton;
import static org.example.view.styling.UIStyle.styleTextField;

public class CollaborativeEditorPanel extends JPanel {
    
    private final Font labelFont = new Font("Arial", Font.BOLD, 14);
    private final Font textFont = new Font("Arial", Font.PLAIN, 14);
    private final Color primaryColor = new Color(70, 130, 180); // Steel Blue
    private final Color foregroundColor = Color.WHITE;
    private JTextArea textArea;

    public CollaborativeEditorPanel() {
        setLayout(new BorderLayout());

        // === Left Sidebar ===
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(400, 800));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // outer padding

        // Section 1: Edit Buttons
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton exportButton = new JButton("Export");
        styleButton(undoButton);
        styleButton(redoButton);
        styleButton(exportButton);

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // Ensure the file has .txt extension
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new File(selectedFile.getPath() + ".txt");
                }
                exportToFile(selectedFile);
            }
        });

        JPanel editSection = createHorizontalSection(undoButton, redoButton, exportButton);
        leftPanel.add(editSection);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 2: Viewer Code
        JLabel viewerLabel = new JLabel("Viewer Code");
        viewerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewerLabel.setFont(labelFont);
        JTextField viewerCode = new JTextField("#yq1xrx", 8);
        viewerCode.setFont(textFont);
        styleTextField(viewerCode);
        viewerCode.setMaximumSize(new Dimension(120, 50)); // prevent it from growing vertically
        viewerCode.setAlignmentX(Component.CENTER_ALIGNMENT); // or LEFT_ALIGNMENT

        styleTextField(viewerCode);

        JButton copyViewer = new JButton("Copy");
        styleButton(copyViewer);
        copyViewer.addActionListener(e -> copyToClipboard(viewerCode.getText()));

        leftPanel.add(viewerLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(createHorizontalSection(viewerCode, copyViewer));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 3: Editor Code
        JLabel editorLabel = new JLabel("Editor Code");
        editorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        editorLabel.setFont(labelFont);
        JTextField editorCode = new JTextField("#yq1xrx", 8);
        editorCode.setFont(textFont);
        styleTextField(editorCode);
        editorCode.setMaximumSize(new Dimension(120, 50)); // prevent it from growing vertically
        editorCode.setAlignmentX(Component.CENTER_ALIGNMENT); // or LEFT_ALIGNMENT

        styleTextField(editorCode);

        JButton copyEditor = new JButton("Copy");
        styleButton(copyEditor);
        copyEditor.addActionListener(e -> copyToClipboard(editorCode.getText()));

        leftPanel.add(editorLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(createHorizontalSection(editorCode, copyEditor));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 4: Active Users
        JLabel activeUsersLabel = new JLabel("Active Users");
        activeUsersLabel.setFont(labelFont);
        activeUsersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JList<String> userList = new JList<>(new String[]{
                "Anonymous Frog (you)", "Anonymous Crab - line 2"
        });
        userList.setFont(textFont);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 100));

        leftPanel.add(activeUsersLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(userScroll);

        // === Editor Area ===
        textArea = new JTextArea("hello");
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane textScroll = new JScrollPane(textArea);

        // Add to main panel
        add(leftPanel, BorderLayout.WEST);
        add(textScroll, BorderLayout.CENTER);
    }

    private JPanel createHorizontalSection(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8)); // internal padding
        for (int i = 0; i < components.length; i++) {
            panel.add(components[i]);
            if (i < components.length - 1) {
                panel.add(Box.createRigidArea(new Dimension(8, 0))); // spacing
            }
        }
        return panel;
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }

    public void exportToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
            JOptionPane.showMessageDialog(this, 
                "File exported successfully!",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error exporting file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error reading file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
