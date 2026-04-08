package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.exceptions.DataAccessException;
import exception.ResponseException;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    // 🔥 Inject real backend
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final GameService gameService;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO, GameService gameService) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.gameService = gameService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Session session = ctx.session;

        try {
            System.out.println("[ws-server] recv: " + ctx.message());
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
            System.out.println("[ws-server] type: " + command.getCommandType());

            switch (command.getCommandType()) {

                case CONNECT -> handleConnectCommand(command, session);

                case MAKE_MOVE -> {
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

    private void handleConnectCommand(UserGameCommand cmd, Session session) throws DataAccessException, IOException
    {
        var username = authDAO.findUsernameByToken(cmd.getAuthToken());
        var gameData = gameDAO.findById(cmd.getGameID());

        if (username == null || gameData == null) {
            sendError(session, "Error: invalid auth or game");
            return;
        }

        int gameID = cmd.getGameID();
        connections.add(gameID, session);

        // 1. Send game to root client
        LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(loadMsg));

        // 2. Notify others
        String message = username + " joined the game";
        NotificationMessage notif = new NotificationMessage(message);
        connections.broadcast(gameID, session, gson.toJson(notif));
    }

    private void handleMakeMove(MakeMoveCommand cmd, Session session) throws IOException, DataAccessException
    {
        var username = authDAO.findUsernameByToken(cmd.getAuthToken());

        if (username == null) {
            sendError(session, "Error: invalid auth");
            return;
        }

        int gameID = cmd.getGameID();
        ChessMove move = cmd.getMove();
        System.out.println("[ws-server] makeMove user=" + username + " game=" + gameID + " move=" + move);

        try {
            // 🔥 REAL GAME LOGIC
            ChessGame updatedGame = gameService.makeMove(cmd.getAuthToken(), gameID, move).game();
            System.out.println("[ws-server] move applied");

            // 1. Broadcast updated board to ALL
            LoadGameMessage loadMsg = new LoadGameMessage(updatedGame);
            connections.broadcastAll(gameID, gson.toJson(loadMsg));

            // 2. Notify others
            String message = username + " made a move";
            NotificationMessage notif = new NotificationMessage(message);
            connections.broadcast(gameID, session, gson.toJson(notif));

        } catch (Exception e) {
            System.out.println("[ws-server] move failed: " + e.getMessage());
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(UserGameCommand cmd, Session session) throws IOException, DataAccessException
    {
        var username = authDAO.findUsernameByToken(cmd.getAuthToken());
        int gameID = cmd.getGameID();

        connections.remove(gameID, session);

        if (username != null) {
            String message = username + " left the game";
            NotificationMessage notif = new NotificationMessage(message);
            connections.broadcast(gameID, session, gson.toJson(notif));
        }
    }

    private void handleResign(UserGameCommand cmd, Session session) throws IOException, DataAccessException
    {
        var username = authDAO.findUsernameByToken(cmd.getAuthToken());
        int gameID = cmd.getGameID();

        try {
            gameService.resign(cmd.getAuthToken(), gameID);

            String message = username + " resigned the game";
            NotificationMessage notif = new NotificationMessage(message);

            connections.broadcastAll(gameID, gson.toJson(notif));

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // =========================
    // HELPERS
    // =========================

    private void sendError(Session session, String errorMsg) {
        try {
            ErrorMessage err = new ErrorMessage(errorMsg);
            session.getRemote().sendString(gson.toJson(err));
        } catch (IOException ignored) {}
    }
}
