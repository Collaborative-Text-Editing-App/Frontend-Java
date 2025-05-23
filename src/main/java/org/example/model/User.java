package org.example.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String id;
    private UserRole role;
    private Cursor cursor;
    private Timestamp lastSeen;

    public User() {
        // For serialization/deserialization
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }


    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = new Timestamp(System.currentTimeMillis());
    }
}
