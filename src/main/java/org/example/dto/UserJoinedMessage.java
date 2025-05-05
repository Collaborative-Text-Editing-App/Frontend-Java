package org.example.dto;

import org.example.model.User;

public class UserJoinedMessage {
    private User user;
    private String documentId;

    public UserJoinedMessage() {} // Required for Jackson

    public UserJoinedMessage(User user, String documentId) {
        this.user = user;
        this.documentId = documentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
