package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.exceptions.UserNotValidatedException;
import io.javalin.websocket.*;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.GameService;
import service.UserService;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    ConnectionManager connectionManager = new ConnectionManager();
    AuthService authService;
    GameService gameService;
    UserService userService;
    Gson serializer = new Gson();

    public WebSocketHandler(AuthService authService, GameService gameService, UserService userService) {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx.session, command);
                case MAKE_MOVE -> makeMove(ctx.session, new Gson().fromJson(ctx.message(), MakeMoveCommand.class));
                case RESIGN -> resign(ctx.session, command);
                case LEAVE -> leave(ctx.session, command);
            }
        } catch (IOException | DataAccessException ex) {
            ErrorMessage message = new ErrorMessage(ex.getMessage());
            ctx.session.getRemote().sendString(serializer.toJson(message));
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(Session session, UserGameCommand command) throws IOException, DataAccessException {
        String username = authService.getUsernameFromToken(command.getAuthToken());
        connectionManager.addToGame(session, command.getGameID(), username);

        ServerMessage message;
        try {
            authService.validateWithToken(command.getAuthToken());
            GameData gameData = gameService.getById(command.getGameID());
            message = new LoadGame(gameData.game());

            String color = getColorForUser(gameData, username);
            connectionManager.broadcast(session, new Notification("User " + username + " connected as " + color), command.getGameID());
        } catch (DataAccessException | NullPointerException e) {
            message = new ErrorMessage("Error: Could not find a game with the given id");
        } catch (UserNotValidatedException e) {
            message = new ErrorMessage("Error: Could not authenticate user");
        }
        session.getRemote().sendString(serializer.toJson(message));
    }

    private void makeMove(Session session, MakeMoveCommand command) throws IOException, DataAccessException {
        String username = authService.getUsernameFromToken(command.getAuthToken());
        ServerMessage message;
        ChessGame game;
        ChessMove move = command.getMove();

        try {
            authService.validateWithToken(command.getAuthToken());
            GameData gameData = gameService.getById(command.getGameID());
            game = gameData.game();
            ChessGame.TeamColor currentTurn = game.getTeamTurn();

            // Ensure it's the user's turn
            if ((currentTurn == ChessGame.TeamColor.WHITE && !Objects.equals(gameData.whiteUsername(), username)) ||
                    (currentTurn == ChessGame.TeamColor.BLACK && !Objects.equals(gameData.blackUsername(), username))) {
                throw new InvalidMoveException("Error: Tried to make a move when it is not their turn");
            }

            // Apply move
            message = new LoadGame(gameService.applyMove(game, move, command.getGameID(), command.getAuthToken()));
            connectionManager.broadcast(null, message, command.getGameID());

            // Notify the move with color
            String playerColor = getColorForUser(gameData, username);
            connectionManager.broadcast(session,
                    new Notification("User " + username + " (" + playerColor + ") made the move " + move),
                    command.getGameID());

            // Check for stalemate
            if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                connectionManager.broadcast(null,
                        new Notification("Game over: Stalemate! Draw."),
                        command.getGameID());
            }

            // Check for checkmate
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                connectionManager.broadcast(null,
                        new Notification("Game over: Checkmate! Black (" + gameData.blackUsername() + ") wins."),
                        command.getGameID());
                return;
            } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connectionManager.broadcast(null,
                        new Notification("Game over: Checkmate! White (" + gameData.whiteUsername() + ") wins."),
                        command.getGameID());
                return;
            }

            // Check notifications
            if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                connectionManager.broadcast(null,
                        new Notification("Check for White (" + gameData.whiteUsername() + ")!"),
                        command.getGameID());
            }
            if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                connectionManager.broadcast(null,
                        new Notification("Check for Black (" + gameData.blackUsername() + ")!"),
                        command.getGameID());
            }

        } catch (UserNotValidatedException e) {
            message = new ErrorMessage("Error: Could not authenticate user");
            session.getRemote().sendString(serializer.toJson(message));
        } catch (DataAccessException e) {
            message = new ErrorMessage("Error: Internal error");
            session.getRemote().sendString(serializer.toJson(message));
        } catch (InvalidMoveException e) {
            message = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(serializer.toJson(message));
        }
    }

    public void resign(Session session, UserGameCommand command) throws DataAccessException, IOException {
        ServerMessage message;
        try {
            authService.validateWithToken(command.getAuthToken());
            GameData gameData = gameService.getById(command.getGameID());
            String username = authService.getUsernameFromToken(command.getAuthToken());

            if (!Objects.equals(gameData.blackUsername(), username) && !Objects.equals(gameData.whiteUsername(), username)) {
                throw new InvalidMoveException("Error: User who is not in the game tried to resign.");
            }

            if (!gameData.game().isActive) {
                throw new InvalidMoveException("Error: Tried to resign, but the game is already over.");
            }

            gameService.markGameAsInactive(command.getGameID());
            String color = getColorForUser(gameData, username);
            message = new Notification("Game over: " + color + " (" + username + ") has resigned.");
            connectionManager.broadcast(null, message, command.getGameID());
        } catch (InvalidMoveException e) {
            message = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(serializer.toJson(message));
        }
    }

    public void leave(Session session, UserGameCommand command) throws IOException {
        ServerMessage message;
        try {
            authService.validateWithToken(command.getAuthToken());
            String username = authService.getUsernameFromToken(command.getAuthToken());

            gameService.removePlayer(username, command.getGameID());
            connectionManager.remove(session);
            message = new Notification(username + " has left the game.");
            connectionManager.broadcast(session, message, command.getGameID());
        } catch (Exception e) {
        }
    }

    // Helper to get color
    private String getColorForUser(GameData gameData, String username) {
        if (Objects.equals(gameData.whiteUsername(), username)) return "White";
        if (Objects.equals(gameData.blackUsername(), username)) return "Black";
        return "Observer";
    }
}
