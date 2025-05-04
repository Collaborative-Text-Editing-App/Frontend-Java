package org.example.service;

import org.example.model.DocumentInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;


public class CollaborativeEditorService {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;
    //private boolean isConnected = false;

    // Configuration and state
    public final String host;
    public final int port;
    // Callbacks
    private Runnable onConnectionLost;

    // Default configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final int RECONNECT_DELAY_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private WebSocketService webSocketService;

    public CollaborativeEditorService(String host, int port) {
        this.host = host != null ? host : DEFAULT_HOST;
        this.port = port > 0 ? port : DEFAULT_PORT;
    }

    public CollaborativeEditorService() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public WebSocketService getWebSocketService() {
        return webSocketService;
    }


    private void handleConnectionError(Exception e) {
        System.err.println("Connection error: " + e.getMessage());
//        cleanUp();
        if (onConnectionLost != null) {
            onConnectionLost.run();
        }
        //attemptReconnect();
    }


    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }


    public synchronized void disconnect() {

        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
        }

        System.out.println("Disconnected from server and WebSocket");

    }


    // Document operations
    public DocumentInfo createNewDoc() throws IOException {
        URL url = new URL("http://localhost:8080/api/documents");
        System.out.println("test1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        int status = con.getResponseCode();
        if (status == 200) {
            System.out.println("THE STATUS IS OF THE HTTP CONNECTION : " + status);
            try (InputStream in = con.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in);
                 BufferedReader br = new BufferedReader(reader)) {
                System.out.println("Reading raw response...");
                String response = br.lines().collect(Collectors.joining());
                System.out.println("Raw JSON response: " + response);
                // Deserialize JSON response into DocumentInfo
                Gson gson = new Gson(); // or Jackson's ObjectMapper
                DocumentInfo info = gson.fromJson(response, DocumentInfo.class);

                webSocketService = new WebSocketService(info.getId());
                webSocketService.connect();

                return info;
            } catch (Exception e) {
                System.err.println("Exception while reading/parsing backend response:");
                e.printStackTrace();
            }
        } else {
            System.err.println("RESPONSE IS NOT 200");
            throw new IOException("Server returned status: " + status);
        }
        return null;
    }

    // TODO:: IMPLEMENT THESE FUNCTIONS
    public DocumentInfo requestSharingCodes() throws IOException {
        return null;
    }

    public DocumentInfo joinDocumentWithCode(String code) throws IOException {


        // Dummy logic – replace with actual API call to resolve document ID from code
        DocumentInfo info = new DocumentInfo(); // Fetch from server properly

        webSocketService = new WebSocketService(info.getId());
        webSocketService.connect();

        return info;
    }
} 