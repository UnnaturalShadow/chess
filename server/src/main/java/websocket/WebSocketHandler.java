package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import org.eclipse.jetty.websocket.api.Session;
import dataaccess.DataAccessException;
import dataaccess.exceptions.UserNotValidatedException;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.messages.Notification;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.ServerMessage;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import model.GameData;

import java.io.IOException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler
{

    ConnectionManager connectionManager = new ConnectionManager();
    AuthService authService;
    GameService gameService;
    UserService userService;
    Gson serializer = new Gson();

    public WebSocketHandler(AuthService authService, GameService gameService, UserService userService)
    {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx)
    {
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws IOException
    {
        try
        {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType())
            {
                case CONNECT -> connect(ctx.session, command);
                case MAKE_MOVE -> makeMove(ctx.session, new Gson().fromJson(ctx.message(), MakeMoveCommand.class));
                case RESIGN -> resign(ctx.session, command);
            }
        }
        catch (IOException | DataAccessException ex)
        {
            ErrorMessage message = new ErrorMessage(ex.getMessage());
            ctx.session.getRemote().sendString(serializer.toJson(message));
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx)
    {
        System.out.println("Websocket closed");
    }

    private void connect(Session session, UserGameCommand command) throws IOException, DataAccessException
    {
        // TODO: Update this to send a load board message to the new player and exclude that player from the notification
        String username = authService.getUsernameFromToken(command.getAuthToken());
        connectionManager.addToGame(session, command.getGameID(), username);
        ServerMessage message;
        try
        {
            authService.validateWithToken(command.getAuthToken());
            message = new LoadGame(gameService.getById(command.getGameID()).game());
            connectionManager.broadcast(session, new Notification("User " + username + " connected"), command.getGameID());
        }
        catch (DataAccessException | NullPointerException e)
        {
            message = new ErrorMessage("Error: Could not find a game with the given id");
        }
        catch (UserNotValidatedException e)
        {
            message = new ErrorMessage("Error: Could not authenticate user");
        }
        session.getRemote().sendString(serializer.toJson(message));
    }

    private void makeMove(Session session, MakeMoveCommand command) throws IOException, DataAccessException
    {
        String username = authService.getUsernameFromToken(command.getAuthToken());
        ServerMessage message;
        ChessGame game;
        ChessMove move = command.getMove();
        try
        {
            authService.validateWithToken(command.getAuthToken());
            GameData gameData = gameService.getById(command.getGameID());
            game = gameData.game();
            if (game.getTeamTurn() == ChessGame.TeamColor.WHITE)
            {
                if (!Objects.equals(gameData.whiteUsername(), username))
                {
                    throw new InvalidMoveException("Error: Tried to make a move when it is not their turn");
                }
            } else
            {
                if (!Objects.equals(gameData.blackUsername(), username))
                {
                    throw new InvalidMoveException("Error: Tried to make a move when it is not their turn");
                }
            }

            message = new LoadGame(gameService.applyMove(game, command.getMove(), command.getGameID(), command.getAuthToken()));

            connectionManager.broadcast(null, message, command.getGameID());
            connectionManager.broadcast(session, new Notification("User " + username + " made the move " + move), command.getGameID());
        }
        catch (UserNotValidatedException e)
        {
            message = new ErrorMessage("Error: Could not authenticate user");
            session.getRemote().sendString(serializer.toJson(message));
        }
        catch (DataAccessException e)
        {
            message = new ErrorMessage("Error: Internal error");
            session.getRemote().sendString(serializer.toJson(message));
        }
        catch (InvalidMoveException e)
        {
            message = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(serializer.toJson(message));
        }
    }

    public void resign(Session session, UserGameCommand command) throws DataAccessException, IOException
    {
        ServerMessage message;
        try
        {
            authService.validateWithToken(command.getAuthToken());
            GameData gameData = gameService.getById(command.getGameID());
            String username = authService.getUsernameFromToken(command.getAuthToken());

            if (!Objects.equals(gameData.blackUsername(), username) && !Objects.equals(gameData.whiteUsername(), username))
            {
                throw new InvalidMoveException("Error: User who is not in the game tried to resign.");
            }

            gameService.markGameAsInactive(command.getGameID());
            message = new Notification(username + " Has resigned.");
            connectionManager.broadcast(null, message, command.getGameID());
        }
        catch (InvalidMoveException e)
        {
            message = new ErrorMessage(e.getMessage());
            session.getRemote().sendString(serializer.toJson(message));
        }

    }
}