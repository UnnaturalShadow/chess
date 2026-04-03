package websocket;

import jakarta.websocket.*;
import jakarta.websocket.Session;

import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketClientEndpoint {
    private Session userSession = null;
    private Consumer<String> messageHandler;

    public WebSocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException("WebSocket connection failed: " + e.getMessage(), e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.userSession = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if (messageHandler != null) messageHandler.accept(message);
    }

    public void addMessageHandler(Consumer<String> msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void send(String message) {
        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendText(message);
        }
    }

    public void close() {
        try {
            if (userSession != null) userSession.close();
        } catch (Exception e) {
            System.out.println("Failed to close WebSocket: " + e.getMessage());
        }
    }
}