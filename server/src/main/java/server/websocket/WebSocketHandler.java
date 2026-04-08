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
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Session session = ctx.session;

        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

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
        String message;
        if (username.equals(gameData.whiteUsername())) {
            message = username + " joined the game as WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            message = username + " joined the game as BLACK";
        } else {
            message = username + " joined the game as an observer";
        }
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

        try {
            ChessGame updatedGame = gameService.makeMove(cmd.getAuthToken(), gameID, move).game();

            // 1. Broadcast updated board to ALL
            LoadGameMessage loadMsg = new LoadGameMessage(updatedGame);
            connections.broadcastAll(gameID, gson.toJson(loadMsg));

            // 2. Notify others
            String message = username + " moved " + describeMove(move);
            NotificationMessage notif = new NotificationMessage(message);
            connections.broadcast(gameID, session, gson.toJson(notif));

            if (updatedGame.isInCheckmate(updatedGame.getTeamTurn())) {
                NotificationMessage state = new NotificationMessage(
                        playerNameForTurn(gameDAO.findById(gameID), updatedGame.getTeamTurn()) + " is in checkmate"
                );
                connections.broadcastAll(gameID, gson.toJson(state));
            } else if (updatedGame.isInStalemate(updatedGame.getTeamTurn())) {
                NotificationMessage state = new NotificationMessage(
                        playerNameForTurn(gameDAO.findById(gameID), updatedGame.getTeamTurn()) + " is in stalemate"
                );
                connections.broadcastAll(gameID, gson.toJson(state));
            } else if (updatedGame.isInCheck(updatedGame.getTeamTurn())) {
                NotificationMessage state = new NotificationMessage(
                        playerNameForTurn(gameDAO.findById(gameID), updatedGame.getTeamTurn()) + " is in check"
                );
                connections.broadcastAll(gameID, gson.toJson(state));
            }

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(UserGameCommand cmd, Session session) throws IOException, DataAccessException
    {
        var username = authDAO.findUsernameByToken(cmd.getAuthToken());
        int gameID = cmd.getGameID();

        var gameData = gameDAO.findById(gameID);
        if (gameData != null && username != null) {
            if (username.equals(gameData.whiteUsername())) {
                gameDAO.update(new model.GameData(
                        gameData.gameID(),
                        null,
                        gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game(),
                        gameData.gameOver()
                ));
            } else if (username.equals(gameData.blackUsername())) {
                gameDAO.update(new model.GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        null,
                        gameData.gameName(),
                        gameData.game(),
                        gameData.gameOver()
                ));
            }
        }

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

    private String describeMove(ChessMove move) {
        return toAlgebraic(move.getStartPosition()) + " to " + toAlgebraic(move.getEndPosition());
    }

    private String toAlgebraic(chess.ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        char rank = (char) ('1' + position.getRow() - 1);
        return "" + file + rank;
    }

    private String playerNameForTurn(model.GameData gameData, ChessGame.TeamColor turn) {
        if (gameData == null) {
            return "A player";
        }
        return switch (turn) {
            case WHITE -> gameData.whiteUsername() != null ? gameData.whiteUsername() : "White";
            case BLACK -> gameData.blackUsername() != null ? gameData.blackUsername() : "Black";
        };
    }
}
