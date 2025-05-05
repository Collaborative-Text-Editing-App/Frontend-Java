package org.example.dto;

public class CursorUpdateMessage {
    private String userId;
    private String documentId;
    private int position;

    public CursorUpdateMessage() { }

    public CursorUpdateMessage(String userId, String documentId, int position, String color) {
        this.userId = userId;
        this.documentId = documentId;
        this.position = position;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}