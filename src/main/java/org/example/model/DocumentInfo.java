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
    private List<User> activeUsers; // Maps userId to their cursor position
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

    public List<User> getActiveUsers() {
        return activeUsers;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

}