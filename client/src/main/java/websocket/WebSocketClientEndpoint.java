package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import exception.ResponseException;
import jakarta.websocket.*;
import java.net.URI;
import java.io.IOException;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketClientEndpoint {
    private Session userSession = null;
    private Consumer<String> messageHandler;
    private final Gson gson = new Gson();

    public WebSocketClientEndpoint(URI endpointURI) throws ResponseException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    "WebSocket connection failed: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.userSession = session;
        session.addMessageHandler(String.class, msg -> {
            if (messageHandler != null) messageHandler.accept(msg);
        });
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

    public void sendCommand(UserGameCommand.CommandType type, String token, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(type, token, gameID);
            userSession.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void sendMakeMoveCommand(String token, int gameID, ChessMove move) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, token, gameID, move.toString());
            userSession.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
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