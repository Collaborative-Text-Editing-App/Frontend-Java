package org.example.model;

// cursor object holds the position of the user, the user id, and the color of the cursor
public class Cursor {
    private int position;
    private String userId;
    private String color;

    public Cursor(int position, String userId, String color) {
        this.position = position;
        this.userId = userId;
        this.color = color;
    }

    @Override
    public String toString() {
        return "position: " + position;
    }


    public Cursor(int position, String userId) {
        this(position, userId, null);
    }

    // Getters and Setters

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}