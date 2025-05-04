package org.example.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

// a document object holds its id, title, codes, active users, and last modified
public class DocumentInfo {
    private String id;
    private String editorCode;   // For users with edit permissions
    private String viewerCode;   // For read-only users
    private String content;  // CRDT-based character list
    private Map<String, Cursor> activeUsers; // Maps userId to their cursor position
    private Timestamp lastModified;

    // Default constructor for deserialization
    public DocumentInfo() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public String getViewerCode() {
        return viewerCode;
    }

     public String getContent() {
         return content;
     }

     public void setContent(String content) {
         this.content = content;
     }

    public Map<String, Cursor> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Map<String, Cursor> activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    // Helper Methods

    public void updateLastModified() {
        this.lastModified = new Timestamp(System.currentTimeMillis());
    }

    public void addActiveUser(String userId, Cursor cursor) {
        activeUsers.put(userId, cursor);
    }

    public void removeActiveUser(String userId) {
        activeUsers.remove(userId);
    }
}