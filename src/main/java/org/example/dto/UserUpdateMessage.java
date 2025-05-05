package org.example.dto;

import org.example.model.User;

import java.util.List;

public class UserUpdateMessage {
    private List<User> users;

    public UserUpdateMessage() {} // Required for Jackson

    public UserUpdateMessage(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
