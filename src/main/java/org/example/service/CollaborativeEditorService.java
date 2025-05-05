package org.example.service;

import org.example.dto.JoinDocumentResponse;
import org.example.model.DocumentInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.example.model.UserRole;


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

                webSocketService = new WebSocketService(info.getId(), info.getActiveUsers().get(info.getActiveUsers().size() - 1).getId());
                webSocketService.connect();
                webSocketService.notifyActiveUsers(info.getActiveUsers().get(info.getActiveUsers().size() - 1));

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

    public DocumentInfo importDoc(String content) throws IOException {
        URL url = new URL("http://localhost:8080/api/documents/import");
        System.out.println("test1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        // Prepare the JSON body.
        String requestBody = "{\"content\":" + new Gson().toJson(content) + "}";

        // Send the body.
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int status = con.getResponseCode();
        System.out.println("THE STATUS IS OF THE HTTP CONNECTION : " + status);
        if (status == 200) {
            try (InputStream in = con.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in);
                 BufferedReader br = new BufferedReader(reader)) {
                System.out.println("Reading raw response...");
                String response = br.lines().collect(Collectors.joining());
                System.out.println("Raw JSON response: " + response);
                Gson gson = new Gson();
                DocumentInfo info = gson.fromJson(response, DocumentInfo.class);

                webSocketService = new WebSocketService(info.getId(), info.getActiveUsers().get(info.getActiveUsers().size() - 1).getId());
                webSocketService.connect();
                webSocketService.notifyActiveUsers(info.getActiveUsers().get(info.getActiveUsers().size() - 1));


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

    public JoinDocumentResponse joinDocumentWithCode(String code) throws IOException {
        String urlStr = "http://localhost:8080/api/documents/" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        URL url = new URL(urlStr);
        System.out.println("test1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        int status = con.getResponseCode();
        System.out.println("THE STATUS IS OF THE HTTP CONNECTION : " + status);
        if (status == 200) {
            try (InputStream in = con.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in);
                 BufferedReader br = new BufferedReader(reader)) {
                System.out.println("Reading raw response...");
                String response = br.lines().collect(Collectors.joining());
                System.out.println("Raw JSON response: " + response);
                // Deserialize JSON response into DocumentInfo
                Gson gson = new Gson(); // or Jackson's ObjectMapper
                JoinDocumentResponse joinResponse = gson.fromJson(response, JoinDocumentResponse.class);
                if (joinResponse.getDocument() != null) {
                    DocumentInfo document = joinResponse.getDocument();
                    webSocketService = new WebSocketService(joinResponse.getDocument().getId(), document.getActiveUsers().get(document.getActiveUsers().size() - 1).getId());
                    webSocketService.connect();
                    webSocketService.notifyActiveUsers(document.getActiveUsers().get(document.getActiveUsers().size() - 1));
                }
                return joinResponse;
            } catch (Exception e) {
                System.err.println("Exception while reading/parsing backend response:");
                e.printStackTrace();
            }
        } else {
            System.err.println("RESPONSE IS NOT 200");
            return null;
        }
        return null;
    }
} 