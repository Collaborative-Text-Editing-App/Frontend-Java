package org.example.service;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.example.dto.DocumentUpdateMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.UUID;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketService {
    private static final String WS_URL = "http://localhost:8080/editor-websocket";
    private static final String TEST_DOCUMENT_ID = "test-doc-123"; // Hardcoded ID for testing
    private StompSession stompSession;
    private StompSessionHandler messageHandler;
    private final String documentId;

    public WebSocketService(String documentId) {
        this.documentId = documentId;
    }

    public WebSocketService() {
        this.userId = UUID.randomUUID().toString();
        this.documentId = TEST_DOCUMENT_ID; // Set hardcoded ID for testing
    }

    public void connect() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient client = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Connected to WebSocket server for document: " + documentId);
                // Subscribe to document-specific updates
                session.subscribe("/topic/documents/" + documentId + "/text-updates", this);
                session.subscribe("/topic/documents/" + documentId + "/user-list", this);
                session.subscribe("/topic/document/" + documentId, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DocumentUpdateMessage.class;
                    }
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        if (payload instanceof DocumentUpdateMessage) {
                            DocumentUpdateMessage update = (DocumentUpdateMessage) payload;

                            // Ignore null/empty content messages
                            documentUpdates.offer(update);
                            System.out.println("Queued valid update: " + update.getContent());
                        } else {
                            System.out.println("Received unexpected payload type: " + payload);
                        }
                    }

                });
            }
        };

        try {
            stompSession = stompClient.connect(WS_URL, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
            System.out.println("Disconnected from WebSocket server");
        }
    }

    public void sendMessage(String destination, Object payload) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.send("/app/documents/" + documentId + destination, payload);
        } else {
            System.err.println("Not connected to WebSocket server");
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public DocumentUpdateMessage getNextDocumentUpdate() {
        try {
            return documentUpdates.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
} 
