package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler handler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.handler = handler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleServerMessage);
        System.out.println("[ws] connected");
    }

    // =========================
    // RECEIVE (Server → Client)
    // =========================
    private void handleServerMessage(String message) {
        System.out.println("[ws] recv: " + message);
        try {
            ServerMessage base = gson.fromJson(message, ServerMessage.class);

            switch (base.getServerMessageType()) {

                case LOAD_GAME -> {
                    LoadGameMessage msg = gson.fromJson(message, LoadGameMessage.class);
                    handler.loadGame(msg.getGame());
                }

                case NOTIFICATION -> {
                    NotificationMessage msg = gson.fromJson(message, NotificationMessage.class);
                    handler.notify(msg.getMessage());
                }

                case ERROR -> {
                    ErrorMessage msg = gson.fromJson(message, ErrorMessage.class);
                    handler.error(msg.getErrorMessage());
                }
            }

        } catch (Exception ex) {
            handler.error("Error: Failed to parse server message");
        }
    }

    // =========================
    // SEND (Client → Server)
    // =========================
    public void connect(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            System.out.println("[ws] send CONNECT " + gson.toJson(cmd));
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, chess.ChessMove move) throws ResponseException {
        try {
            var cmd = new MakeMoveCommand(authToken, gameID, move);
            System.out.println("[ws] send MAKE_MOVE " + gson.toJson(cmd));
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void leave(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            System.out.println("[ws] send LEAVE " + gson.toJson(cmd));
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        try {
            var cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            System.out.println("[ws] send RESIGN " + gson.toJson(cmd));
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }
}
