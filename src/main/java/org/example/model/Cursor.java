package org.example.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

// cursor object holds the position of the user, the user id, and the color of the cursor
public class Cursor {
    private int position;
    private String color;

    public Cursor() {// Random color
    }

    // Getters and Setters
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
