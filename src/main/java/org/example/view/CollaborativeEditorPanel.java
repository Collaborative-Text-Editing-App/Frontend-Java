package org.example.view;

import org.example.controller.CollaborativeEditorController;
import org.example.model.DocumentInfo;
import org.example.dto.DocumentUpdateMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static org.example.view.styling.UIStyle.styleButton;
import static org.example.view.styling.UIStyle.styleTextField;

public class CollaborativeEditorPanel extends JPanel {
    
    private final Font labelFont = new Font("Arial", Font.BOLD, 14);
    private final Font textFont = new Font("Arial", Font.PLAIN, 14);
    private final Color primaryColor = new Color(70, 130, 180); // Steel Blue
    private final Color foregroundColor = Color.WHITE;
    private JTextArea textArea;
    private final DocumentInfo documentInfo;
    private boolean isRemoteUpdate = false;
    private final CollaborativeEditorController controller;
    private boolean isPaste = false;
    private boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_THRESHOLD = 100; // milliseconds between keystrokes

    public CollaborativeEditorPanel(DocumentInfo documentInfo, CollaborativeEditorController collaborativeEditorController) {
        this.documentInfo = documentInfo;
        this.controller = collaborativeEditorController;
        setLayout(new BorderLayout());

        // Request initial document state
        requestInitialDocumentState();
        
        // Start a thread to handle document updates
        new Thread(() -> {
            while (true) {
                DocumentUpdateMessage update = this.controller.getDocumentUpdates();
                System.out.println("Received update: " + update);
                if (update != null) {
                    SwingUtilities.invokeLater(() -> {
                        if (!textArea.getText().equals(update.getContent())) {
                            isRemoteUpdate = true;
                            textArea.setText(update.getContent());
                            isRemoteUpdate = false;
                        }
                    });
                }
            }
        }).start();

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

        undoButton.addActionListener(e -> controller.undoAction());

        redoButton.addActionListener(e -> controller.redoAction());

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
        JTextField viewerCode = new JTextField(documentInfo.getViewerCode(), 8);
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
        JTextField editorCode = new JTextField(documentInfo.getEditorCode(), 8);
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

        JList<String> userList = new JList<>(
                documentInfo.getActiveUsers()
                        .stream()
                        .map(user ->
                                "ID: " + user.getUserId() +
                                        ", Role: " + user.getRole() +
                                        ", Cursor: pos " + (user.getCursor() != null ? user.getCursor().getPosition() : "-") +
                                        ", Connected: " + user.isConnected()
                        )
                        .toArray(String[]::new)
        );

        userList.setFont(textFont);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 100));

        leftPanel.add(activeUsersLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(userScroll);

        // === Editor Area ===
        textArea = new JTextArea(documentInfo.getContent());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setPreferredSize(new Dimension(800, 800));
        textScroll.setMinimumSize(new Dimension(600, 600));
        // Add key bindings for paste
        textArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), "paste-from-clipboard");
        textArea.getActionMap().put("paste-from-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaste = true;
                textArea.paste();
            }
        });

        // Add key listener to detect typing
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_CONTROL && 
                    e.getKeyCode() != KeyEvent.VK_SHIFT && 
                    e.getKeyCode() != KeyEvent.VK_ALT) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTypingTime > TYPING_THRESHOLD) {
                        isTyping = true;
                    }
                    lastTypingTime = currentTime;
                }
            }
        });

        // Add text change listener to send updates to WebSocket
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (isRemoteUpdate) return;
                try {
                    int offset = e.getOffset();
                    int length = e.getLength();
                    String newText = textArea.getText(offset, length);

                    controller.insertText(newText, offset, isPaste, isTyping);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (isRemoteUpdate) {
                    return;
                }
                try {
                    int offset = e.getOffset();
                    int length = e.getLength();
                    controller.removeText(offset, length, textArea);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                sendTextUpdate();
            }

            private void sendTextUpdate() {
//                if (webSocketService != null) {
//                    webSocketService.sendMessage("/app/text-update", textArea.getText());
//                }
            }
        });

        // Add to main panel
        add(leftPanel, BorderLayout.WEST);
        add(textScroll, BorderLayout.CENTER);
    }

    private void requestInitialDocumentState() {
        // Send a request to get the current document state
        controller.requestInitialText();
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
