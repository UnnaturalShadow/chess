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


import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler
{

    ConnectionManager connectionManager = new ConnectionManager();
    AuthService authService;
    GameService gameService;
    UserService userService;
    Gson serializer = new Gson();

    public WebSocketHandler(AuthService authService, GameService gameService)
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
            message = new LoadGame(gameService.getById(command.getGameID()));
            connectionManager.broadcast(session, new Notification("User " + username + " connected"), command.getGameID());
        }
        catch (DataAccessException e)
        {
            message = new ErrorMessage("Error: Could not find a game with the given id");
        }
        catch (UserNotValidatedException e)
        {
            message = new ErrorMessage("Error: Could not authenticate user");
        }
        session.getRemote().sendString(serializer.toJson(message));
    }

    private void makeMove(Session session, UserGameCommand command)
    {
        System.out.println("Make Move called");
    }
}