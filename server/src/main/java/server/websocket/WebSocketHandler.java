package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Session session = ctx.session;

        try {
            // 1. Parse base command
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (command.getCommandType()) {

                case CONNECT -> handleConnectCommand(command, session);

                case MAKE_MOVE -> {
                    // reparse into subclass
                    MakeMoveCommand moveCmd = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    handleMakeMove(moveCmd, session);
                }

                case LEAVE -> handleLeave(command, session);

                case RESIGN -> handleResign(command, session);
            }

        } catch (Exception ex) {
            sendError(session, "Error: " + ex.getMessage());
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WebSocket closed");
    }

    // =========================
    // COMMAND HANDLERS
    // =========================

    private void handleConnectCommand(UserGameCommand cmd, Session session) throws IOException {
        int gameID = cmd.getGameID();

        connections.add(gameID, session);

        // 1. Send LOAD_GAME to root client
        LoadGameMessage loadMsg = new LoadGameMessage(getGame(gameID));
        session.getRemote().sendString(gson.toJson(loadMsg));

        // 2. Notify others
        NotificationMessage notif = new NotificationMessage("A user connected to the game");
        connections.broadcast(gameID, session, gson.toJson(notif));
    }

    private void handleMakeMove(MakeMoveCommand cmd, Session session) throws IOException {
        int gameID = cmd.getGameID();

        // TODO: validate + update game
        // updateGame(cmd.getMove());

        // 1. Send updated game to ALL clients
        LoadGameMessage loadMsg = new LoadGameMessage(getGame(gameID));
        connections.broadcastAll(gameID, gson.toJson(loadMsg));

        // 2. Notify others
        NotificationMessage notif = new NotificationMessage("A move was made");
        connections.broadcast(gameID, session, gson.toJson(notif));
    }

    private void handleLeave(UserGameCommand cmd, Session session) throws IOException {
        int gameID = cmd.getGameID();

        connections.remove(gameID, session);

        NotificationMessage notif = new NotificationMessage("A user left the game");
        connections.broadcast(gameID, session, gson.toJson(notif));
    }

    private void handleResign(UserGameCommand cmd, Session session) throws IOException {
        int gameID = cmd.getGameID();

        // TODO: mark game over in DB

        NotificationMessage notif = new NotificationMessage("A player resigned");
        connections.broadcastAll(gameID, gson.toJson(notif));
    }

    // =========================
    // HELPERS
    // =========================

    private void sendError(Session session, String errorMsg) {
        try {
            ErrorMessage err = new ErrorMessage(errorMsg);
            session.getRemote().sendString(gson.toJson(err));
        } catch (IOException ignored) {
        }
    }

    // You will replace this with your real game retrieval
    private ChessGame getGame(int gameID) {
        return null;
    }
}