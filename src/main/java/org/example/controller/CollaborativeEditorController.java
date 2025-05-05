package org.example.controller;

import org.example.dto.DocumentUpdateMessage;
import org.example.dto.JoinDocumentResponse;
import org.example.dto.TextOperationMessage;
import org.example.model.DocumentInfo;
import org.example.service.CollaborativeEditorService;
import org.example.service.WebSocketService;
import org.example.view.CollaborativeEditorPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CollaborativeEditorController {

    private final CollaborativeEditorService collaborationService;
    private JTextArea textArea;
    private final List<String> activeUsers;
    private String viewerCode;
    private String editorCode;
    private final JFrame homeScreen;
    public CollaborativeEditorController(String host, int port, JFrame homeScreen) {
        this.collaborationService = new CollaborativeEditorService(host, port);
        this.activeUsers = new ArrayList<>();
        this.homeScreen = homeScreen;
        setupMessageHandler();
    }

    public DocumentUpdateMessage getDocumentUpdates(){
        return this.collaborationService.getWebSocketService().getNextDocumentUpdate();
    }

    public void insertText(String newText, int offset, boolean isPaste, boolean isTyping) {
        TextOperationMessage message = new TextOperationMessage();
        message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());  
        message.setOperationType("INSERT");
        message.setPosition(offset);
        message.setUserId(collaborationService.getWebSocketService().getUserId());
        message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());

        if (isPaste || !isTyping) {
            // Send the whole string for paste or non-typing operations
            message.setText(newText);
            collaborationService.getWebSocketService().sendMessage("/app/document.edit", message);
            isPaste = false;
            isTyping = false;
        } else {
            // Send character by character for typing
            for (int i = 0; i < newText.length(); i++) {
                TextOperationMessage charMessage = new TextOperationMessage();
                charMessage.setOperationType("INSERT");
                charMessage.setPosition(offset + i);
                charMessage.setText(String.valueOf(newText.charAt(i)));
                charMessage.setUserId(message.getUserId());
                charMessage.setDocumentId(message.getDocumentId());
                collaborationService.getWebSocketService().sendMessage("/app/document.edit", charMessage);
            }
        }
    }

    public void removeText(int offset, int length) {
        if (textArea == null) {
            System.err.println("TextArea is not initialized");
            return;
        }
        
        try {
            System.out.println("Removing text at offset: " + offset + " with length: " + length);
                    
            if (length == 1) {
                // Single character deletion (backspace or delete key)
                TextOperationMessage message = new TextOperationMessage();
                message.setOperationType("DELETE");
                message.setPosition(offset);
                message.setUserId(collaborationService.getWebSocketService().getUserId());
                message.setLength(length);  
                message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());
                collaborationService.getWebSocketService().sendMessage("/app/document.edit", message);
            } else {
                // Multiple characters deletion (selection)
                System.out.println("Multiple characters deletion (selection)");
                //List<TextOperationMessage> deleteOperations = new ArrayList<>();
                System.out.println("Deleting " + length + " characters at offset " + offset);
                TextOperationMessage message = new TextOperationMessage();
                message.setOperationType("DELETE");
                message.setPosition(offset);
                message.setUserId(collaborationService.getWebSocketService().getUserId());
                message.setLength(1);       
                message.setTextLength(length);
                message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());
                //deleteOperations.add(message);
                // Send all delete operations as a batch
                System.out.println("Sending message: " + message);
                collaborationService.getWebSocketService().sendMessage("/app/document.edit", message);
                System.out.println("Message sent");

                // for (TextOperationMessage op : deleteOperations) {
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestInitialText(){
        TextOperationMessage message = new TextOperationMessage();
        message.setOperationType("GET_STATE");
        message.setUserId(collaborationService.getWebSocketService().getUserId());
        message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());
        collaborationService.getWebSocketService().sendMessage("/app/document.edit", message);
    }

    public void undoAction(){
        TextOperationMessage undoMsg = new TextOperationMessage();
        undoMsg.setDocumentId(collaborationService.getWebSocketService().getDocumentId()); // or get from webSocketService
        collaborationService.getWebSocketService().sendMessage("/app/document/undo", undoMsg);

    }

    public void redoAction(){
        TextOperationMessage redoMsg = new TextOperationMessage();
        redoMsg.setDocumentId(collaborationService.getWebSocketService().getDocumentId()); // or get from webSocketService
        collaborationService.getWebSocketService().sendMessage("/app/document/redo", redoMsg);
    }

    public void removeText(int offset){
        // Create and send deletion message
        TextOperationMessage message = new TextOperationMessage();
        message.setOperationType("DELETE");
        message.setPosition(offset);
        message.setUserId(collaborationService.getWebSocketService().getUserId());
        message.setDocumentId(collaborationService.getWebSocketService().getDocumentId());

        // Send to the endpoint for CRDT operations
        collaborationService.getWebSocketService().sendMessage("/app/document.edit", message);
    }
    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    private void setupMessageHandler() {
        collaborationService.setOnMessageReceived(message -> {
            if (message.startsWith("TEXT:")) {
                updateText(message.substring(5));
            } else if (message.startsWith("USERS:")) {
                updateActiveUsers(message.substring(6));
            }
        });
    }

    public void sendTextUpdate(String text) {
//        if (collaborationService.isConnected()) {
//            collaborationService.sendMessage("TEXT:" + text);
//        }
    }

    private void updateText(String newText) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> textArea.setText(newText));
        }
    }

    private void updateActiveUsers(String usersList) {
        String[] users = usersList.split(",");
        activeUsers.clear();
        for (String user : users) {
            if (!user.trim().isEmpty()) {
                activeUsers.add(user.trim());
            }
        }
    }

    public List<String> getActiveUsers() {
        return new ArrayList<>(activeUsers);
    }

    public String getViewerCode() {
        return viewerCode;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public void disconnect() {
        collaborationService.disconnect();
    }

    public void createNewDocument() {

        try {
            DocumentInfo documentInfo = collaborationService.createNewDoc();

            SwingUtilities.invokeLater(() -> {
                // Pass WebSocketService into the editor panel
                CollaborativeEditorPanel editorPanel = new CollaborativeEditorPanel(documentInfo, this);
                JFrame editorFrame = new JFrame("Collaborative Editor - " + documentInfo.getId());
                editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editorFrame.add(editorPanel);
                editorFrame.pack();
                editorFrame.setLocationRelativeTo(homeScreen);
                editorFrame.setVisible(true);
                if (homeScreen != null) homeScreen.dispose();
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                if (homeScreen != null) {
                    JOptionPane.showMessageDialog(homeScreen,
                            "Error creating document: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    public void openImportedDocument(String content) {
        try {
            DocumentInfo documentInfo = collaborationService.importDoc(content);
            SwingUtilities.invokeLater(() -> {
                // Pass WebSocketService into the editor panel
                CollaborativeEditorPanel editorPanel = new CollaborativeEditorPanel(documentInfo, this);
                JFrame editorFrame = new JFrame("Collaborative Editor - " + documentInfo.getId());
                editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editorFrame.add(editorPanel);
                editorFrame.pack();
                editorFrame.setLocationRelativeTo(homeScreen);
                editorFrame.setVisible(true);
                if (homeScreen != null) homeScreen.dispose();
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                if (homeScreen != null) {
                    JOptionPane.showMessageDialog(homeScreen,
                            "Error creating document: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    public void joinDoc(String code) {
        try {
            JoinDocumentResponse joinResponse = collaborationService.joinDocumentWithCode(code);
            SwingUtilities.invokeLater(() -> {
                CollaborativeEditorPanel editorPanel = new CollaborativeEditorPanel(joinResponse.getDocument(), this);
                JFrame editorFrame = new JFrame("Collaborative Editor - " + joinResponse.getDocument().getId());
                editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editorFrame.add(editorPanel);
                editorFrame.pack();
                editorFrame.setLocationRelativeTo(homeScreen);
                editorFrame.setVisible(true);
                if (homeScreen != null) homeScreen.dispose();
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                if (homeScreen != null) {
                    JOptionPane.showMessageDialog(homeScreen,
                            "Error joining document: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}
