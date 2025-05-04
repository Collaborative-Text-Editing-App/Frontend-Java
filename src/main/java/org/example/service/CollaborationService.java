package org.example.service;


import org.example.model.DocumentInfo;

public interface CollaborationService {
    void startNewSession() throws Exception;
    DocumentInfo requestSharingCodes() throws Exception;
    void connectWebSocket() throws Exception;
}
