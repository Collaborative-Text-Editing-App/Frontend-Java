package org.example.dto;

import org.example.model.DocumentInfo;
import org.example.model.UserRole;

public class JoinDocumentResponse {
    private DocumentInfo document;
    private UserRole role;

    public DocumentInfo getDocument() { return document; }
    public UserRole getRole() { return role; }
}
