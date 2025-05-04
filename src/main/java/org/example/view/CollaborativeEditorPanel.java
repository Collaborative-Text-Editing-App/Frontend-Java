package org.example.view;

import org.example.service.WebSocketService;
import org.example.dto.TextOperationMessage;
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
    private final WebSocketService webSocketService;
    private boolean isRemoteUpdate = false;
    private boolean isPaste = false;
    private boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_THRESHOLD = 100; // milliseconds between keystrokes

    public CollaborativeEditorPanel(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
        setLayout(new BorderLayout());

        // Request initial document state
        requestInitialDocumentState();
        
        // Start a thread to handle document updates
        new Thread(() -> {
            while (true) {
                DocumentUpdateMessage update = webSocketService.getNextDocumentUpdate();
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

        undoButton.addActionListener(e -> {
            TextOperationMessage undoMsg = new TextOperationMessage();
            System.out.println("Sending undo message");
            undoMsg.setDocumentId("test-doc-123"); // or get from webSocketService
            webSocketService.sendMessage("/document/undo", undoMsg);
        });

        redoButton.addActionListener(e -> {
            TextOperationMessage redoMsg = new TextOperationMessage();
            redoMsg.setDocumentId("test-doc-123"); // or get from webSocketService
            webSocketService.sendMessage("/document/redo", redoMsg);
        });

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
        textArea = new JTextArea("");
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane textScroll = new JScrollPane(textArea);

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
                    
                    TextOperationMessage message = new TextOperationMessage();
                    message.setOperationType("INSERT");
                    message.setPosition(offset);
                    message.setUserId(webSocketService.getUserId());
                    message.setDocumentId("test-doc-123");

                    if (isPaste || !isTyping) {
                        // Send the whole string for paste or non-typing operations
                        message.setText(newText);
                        webSocketService.sendMessage("/document.edit", message);
                        isPaste = false;
                        isTyping = false;
                    } else {
                        // Send character by character for typing
                        for (int i = 0; i < newText.length(); i++) {
                            TextOperationMessage charMessage = new TextOperationMessage();
                            charMessage.setOperationType("INSERT");
                            charMessage.setPosition(offset + i);
                            charMessage.setText(String.valueOf(newText.charAt(i)));
                            charMessage.setUserId(webSocketService.getUserId());
                            charMessage.setDocumentId("test-doc-123");
                            webSocketService.sendMessage("/document.edit", charMessage);
                        }
                    }
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
                    String deletedText = textArea.getText(offset, length);
                    
                    if (length == 1) {
                        // Single character deletion (backspace or delete key)
                        TextOperationMessage message = new TextOperationMessage();
                        message.setOperationType("DELETE");
                        message.setPosition(offset);
                        message.setText(deletedText);
                        message.setUserId(webSocketService.getUserId());
                        message.setLength(length);  
                        message.setDocumentId(webSocketService.getDocumentId());
                        webSocketService.sendMessage("/document.edit", message);
                    } else {
                        // Multiple characters deletion (selection)
                        List<TextOperationMessage> deleteOperations = new ArrayList<>();
                        for (int i = 0; i < length; i++) {
                            TextOperationMessage message = new TextOperationMessage();
                            message.setOperationType("DELETE");
                            message.setPosition(offset + i);
                            message.setText(String.valueOf(deletedText.charAt(i)));
                            message.setUserId(webSocketService.getUserId());
                            message.setLength(length);
                            message.setDocumentId(webSocketService.getDocumentId());
                            deleteOperations.add(message);
                        }
                        // Send all delete operations as a batch
                        for (TextOperationMessage op : deleteOperations) {
                            webSocketService.sendMessage("/document.edit", op);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                // This is for style changes, which we don't need to handle
            }
        });

        // Add to main panel
        add(leftPanel, BorderLayout.WEST);
        add(textScroll, BorderLayout.CENTER);
    }

    private void requestInitialDocumentState() {
        // Send a request to get the current document state
        TextOperationMessage message = new TextOperationMessage();
        message.setOperationType("GET_STATE");
        message.setUserId(webSocketService.getUserId());
        message.setDocumentId(webSocketService.getDocumentId());
        webSocketService.sendMessage("/document.edit", message);
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
