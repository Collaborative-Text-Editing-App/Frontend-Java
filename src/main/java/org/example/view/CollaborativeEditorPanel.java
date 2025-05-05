package org.example.view;

import org.example.controller.CollaborativeEditorController;
import org.example.dto.UserJoinedMessage;
import org.example.dto.UserUpdateMessage;
import org.example.model.DocumentInfo;
import org.example.dto.DocumentUpdateMessage;
import org.example.model.UserRole;
import org.example.model.User;
import org.example.dto.TextOperationMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

import static org.example.view.styling.UIStyle.styleButton;
import static org.example.view.styling.UIStyle.styleTextField;

public class CollaborativeEditorPanel extends JPanel {

    private final Font labelFont = new Font("Arial", Font.BOLD, 14);
    private final Font textFont = new Font("Arial", Font.PLAIN, 14);
    private final Color primaryColor = new Color(70, 130, 180);
    private final Color foregroundColor = Color.WHITE;
    private JTextArea textArea;
    private final DocumentInfo documentInfo;
    private boolean isRemoteUpdate = false;
    private final CollaborativeEditorController controller;
    private boolean isPaste = false;
    private boolean isTyping = false;
    private long lastTypingTime = 0;
    private static final long TYPING_THRESHOLD = 100;

    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    @Override
    public void addNotify() {
        super.addNotify();
        // Get the parent JFrame
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        if (parentFrame != null) {
            // Add window listener to the JFrame
            parentFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("CollaborativeEditorPanel is closing from inside the panel (using addNotify).");
                    controller.handleClose();
                }
            });
        }
    }
    public CollaborativeEditorPanel(DocumentInfo documentInfo, UserRole role, CollaborativeEditorController collaborativeEditorController) {
        this.documentInfo = documentInfo;
        this.controller = collaborativeEditorController;
        setLayout(new BorderLayout());

        requestInitialDocumentState();

        // Thread to handle remote text updates
        new Thread(() -> {
            while (true) {
                DocumentUpdateMessage update = this.controller.getDocumentUpdates();
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

        // Thread to handle user list updates
        new Thread(() -> {
            while (true) {
                UserUpdateMessage update = this.controller.getUserJoinedUpdates();
                if (update != null) {
                    SwingUtilities.invokeLater(() -> updateUserList(update.getUsers()));
                }
            }
        }).start();

        // === Left Sidebar ===
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(400, 800));
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

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
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new File(selectedFile.getPath() + ".txt");
                }
                exportToFile(selectedFile);
            }
        });

        leftPanel.add(createHorizontalSection(undoButton, redoButton, exportButton));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));


        if (role == UserRole.EDITOR) {
            // Section 2: Viewer Code
            JLabel viewerLabel = new JLabel("Viewer Code");
            viewerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            viewerLabel.setFont(labelFont);
            JTextField viewerCode = new JTextField(documentInfo.getViewerCode(), 8);
            viewerCode.setFont(textFont);
            styleTextField(viewerCode);
            viewerCode.setMaximumSize(new Dimension(120, 50));
            viewerCode.setAlignmentX(Component.CENTER_ALIGNMENT);

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
            editorCode.setMaximumSize(new Dimension(120, 50));
            editorCode.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton copyEditor = new JButton("Copy");
            styleButton(copyEditor);
            copyEditor.addActionListener(e -> copyToClipboard(editorCode.getText()));

            leftPanel.add(editorLabel);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            leftPanel.add(createHorizontalSection(editorCode, copyEditor));
            leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        // Active Users (dynamic list)
        JLabel activeUsersLabel = new JLabel("Active Users");
        activeUsersLabel.setFont(labelFont);
        activeUsersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(textFont);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 100));
        leftPanel.add(activeUsersLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(userScroll);

        // Populate initial user list
        updateUserList(documentInfo.getActiveUsers());

        // === Editor Area ===

        textArea = new JTextArea(documentInfo.getContent());
        controller.setTextArea(textArea);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(role == UserRole.EDITOR);  // This disables editing for VIEWER
        textArea.setLineWrap(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setPreferredSize(new Dimension(800, 800));
        textScroll.setMinimumSize(new Dimension(600, 600));
        
        // Set the textArea in the controller
        controller.setTextArea(textArea);
        
        // Add key bindings for paste
        textArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), "paste-from-clipboard");
        textArea.getActionMap().put("paste-from-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaste = true;
                textArea.paste();
            }
        });

//        textArea.addCaretListener(e -> {
//            SwingUtilities.invokeLater(() -> {
//                try {
//                    int caretPosition = textArea.getCaretPosition();
//                    Rectangle2D r = textArea.modelToView2D(caretPosition);
//                    if (r != null) {
//                        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
//                        int visualLine = (int)(r.getY() / lineHeight);
//                        controller.sendCursorUpdate(visualLine);
//                    } else {
//                        int logicalLine = textArea.getLineOfOffset(caretPosition);
//                        controller.sendCursorUpdate(logicalLine);
//                    }
//                } catch (BadLocationException ex) {
//                    ex.printStackTrace();
//                }
//            });
//        });
        // Typing detection
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

        // Text update listener
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
                if (isRemoteUpdate) return;
                try {
                    int offset = e.getOffset();
                    int length = e.getLength();
                    //String deletedText = textArea.getText(offset, length);
                    controller.removeText(offset, length);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                // Not needed for plain text components
            }
        });

        add(leftPanel, BorderLayout.WEST);
        add(textScroll, BorderLayout.CENTER);
    }

    private void requestInitialDocumentState() {
        controller.requestInitialText();
    }

    private JPanel createHorizontalSection(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < components.length; i++) {
            panel.add(components[i]);
            if (i < components.length - 1) {
                panel.add(Box.createRigidArea(new Dimension(8, 0)));
            }
        }
        return panel;
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
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
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserList(List<User> users) {
        userListModel.clear();
        for (User user : users) {
            userListModel.addElement(formatUser(user));
        }
    }

    private String formatUser(User user) {
        String id = "";
        if (user != null && user.getId() != null) {
            id =  user.getId().substring(0, 4);
        }
        return "ID: " + id +
                ", Role: " + user.getRole() +
                ", Cursor: Line " + (user.getCursor() != null ? user.getCursor().getPosition() + 1: "-") +
                ", Color :" + (user.getCursor().getColor());
    }
}
