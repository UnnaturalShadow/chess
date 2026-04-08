package client.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final NotificationHandler handler;
    private final Gson gson = new Gson();
    private final CountDownLatch openLatch = new CountDownLatch(1);

    public WebSocketFacade(String url, NotificationHandler handler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.handler = handler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            if (!openLatch.await(2, TimeUnit.SECONDS)) {
                throw new ResponseException(ResponseException.Code.ServerError, "WebSocket connection timed out");
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handleServerMessage(message);
            }
        });
        System.out.println("[ws] connected");
        openLatch.countDown();
    }

    // =========================
    // RECEIVE (Server → Client)
    // =========================
    private void handleServerMessage(String message) {
        System.out.println("[ws] recv: " + message);
        try {
            JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
            String type = obj.get("serverMessageType").getAsString();

            switch (ServerMessage.ServerMessageType.valueOf(type)) {

                case LOAD_GAME -> {
                    LoadGameMessage msg = gson.fromJson(message, LoadGameMessage.class);
                    System.out.println("[ws] load_game");
                    handler.loadGame(msg.getGame());
                }

                case NOTIFICATION -> {
                    NotificationMessage msg = gson.fromJson(message, NotificationMessage.class);
                    System.out.println("[ws] notification");
                    handler.notify(msg.getMessage());
                }

                case ERROR -> {
                    ErrorMessage msg = gson.fromJson(message, ErrorMessage.class);
                    System.out.println("[ws] server_error");
                    handler.error(msg.getErrorMessage());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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
