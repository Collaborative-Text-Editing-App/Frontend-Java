package org.example.model;

import java.security.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// a document object holds its id, title, codes, active users, and last modified
public class DocumentInfo {
    private String id;
    private String title;
    private String editorCode;   // For users with edit permissions
    private String viewerCode;   // For read-only users
    // private List<CRDTChar> content;  // CRDT-based character list
    private Map<String, Cursor> activeUsers; // Maps userId to their cursor position
    private Timestamp lastModified;

    public void Document(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.editorCode = UUID.randomUUID().toString().substring(0, 8);
        this.viewerCode = UUID.randomUUID().toString().substring(0, 8);
        //this.content = new ArrayList<>();
        this.activeUsers = new HashMap<>();
        this.lastModified = new Timestamp(Date.from(Instant.now()), null);
    }

    // Default constructor for deserialization
    public void Document() {
        this.title = "Untitled";
        this.editorCode = UUID.randomUUID().toString().substring(0, 8);
        this.viewerCode = UUID.randomUUID().toString().substring(0, 8);
        //this.content = new ArrayList<>();
        this.activeUsers = new HashMap<>();
        this.lastModified = new Timestamp(Date.from(Instant.now()), null);
        this.id = UUID.randomUUID().toString();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public String getViewerCode() {
        return viewerCode;
    }

    // public List<CRDTChar> getContent() {
    //     return content;
    // }

    // public void setContent(List<CRDTChar> content) {
    //     this.content = content;
    // }

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
        this.lastModified = new Timestamp(Date.from(Instant.now()), null);
    }

    public void addActiveUser(String userId, Cursor cursor) {
        activeUsers.put(userId, cursor);
    }

    public void removeActiveUser(String userId) {
        activeUsers.remove(userId);
    }

    // public void insertChar(CRDTChar crdtChar, int index) {
    //     content.add(index, crdtChar);
    //     updateLastModified();
    // }

    // public void deleteChar(int index) {
    //     if (index >= 0 && index < content.size()) {
    //         content.get(index).setVisible(false);
    //         updateLastModified();
    //     }
    // }
}