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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebSocketService {
    private static final String WS_URL = "http://localhost:8080/editor-websocket";
    private StompSession stompSession;
    private StompSessionHandler messageHandler;
    private final String documentId;

    public WebSocketService(String documentId) {
        this.documentId = documentId;
    }

    public void setMessageHandler(StompSessionHandler handler) {
        this.messageHandler = handler;
    }

    public void connect() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient client = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (messageHandler != null) {
                    messageHandler.handleFrame(headers, payload);
                } else {
                    System.out.println("Received message: " + payload);
                }
            }

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Connected to WebSocket server for document: " + documentId);
                // Subscribe to document-specific updates
                session.subscribe("/topic/documents/" + documentId + "/text-updates", this);
                session.subscribe("/topic/documents/" + documentId + "/user-list", this);
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
}
