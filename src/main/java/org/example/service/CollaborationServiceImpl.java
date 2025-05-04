package org.example.service;


import org.example.model.DocumentInfo;

public class CollaborationServiceImpl implements CollaborationService {
    @Override
    public void startNewSession() throws Exception {
        // Implementation with HTTP calls
    }
    
    @Override
    public DocumentInfo requestSharingCodes() throws Exception {
        // Implementation with HTTP calls
        return new DocumentInfo();
    }
    
    @Override
    public void connectWebSocket() throws Exception {
        // WebSocket connection logic
    }
}
