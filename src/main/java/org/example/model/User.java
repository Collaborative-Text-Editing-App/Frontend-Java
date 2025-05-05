package org.example.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private UserRole role;
    private Cursor cursor;
    private boolean connected;
    private Timestamp lastSeen;

    public User() {
        // For serialization/deserialization
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
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

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = new Timestamp(System.currentTimeMillis());
    }
}
