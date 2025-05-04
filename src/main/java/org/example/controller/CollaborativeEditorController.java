package org.example.controller;


import org.example.service.CollaborativeEditorService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CollaborativeEditorController {
    private CollaborativeEditorService editorService;
    private JTextArea textArea;
    private List<String> activeUsers;
    private String viewerCode;
    private String editorCode;

    public CollaborativeEditorController(String host, int port) {
        this.editorService = new CollaborativeEditorService(host, port);
        this.activeUsers = new ArrayList<>();
        setupMessageHandler();
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    private void setupMessageHandler() {
        editorService.setOnMessageReceived(message -> {
            // Handle different types of messages
            if (message.startsWith("TEXT:")) {
                updateText(message.substring(5));
            } else if (message.startsWith("USERS:")) {
                updateActiveUsers(message.substring(6));
            }
        });
    }

    public void sendTextUpdate(String text) {
        if (editorService.isConnected()) {
            editorService.sendTextUpdate("TEXT:" + text);
        }
    }

    private void updateText(String newText) {
        if (textArea != null) {
            SwingUtilities.invokeLater(() -> {
                textArea.setText(newText);
            });
        }
    }

    private void updateActiveUsers(String usersList) {
        String[] users = usersList.split(",");
        activeUsers.clear();
        for (String user : users) {
            activeUsers.add(user.trim());
        }
    }

    public List<String> getActiveUsers() {
        return new ArrayList<>(activeUsers);
    }

    public void setViewerCode(String code) {
        this.viewerCode = code;
    }

    public void setEditorCode(String code) {
        this.editorCode = code;
    }

    public String getViewerCode() {
        return viewerCode;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public void disconnect() {
        editorService.disconnect();
    }
} 