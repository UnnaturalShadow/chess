package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import dataaccess.DataAccessException;
import service.AuthService;
import service.GameService;
import websocket.messages.Notification;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler
{

    ConnectionManager connectionManager = new ConnectionManager();
    AuthService authService;
    GameService gameService;
    Gson serializer = new Gson();

    public WebSocketHandler(AuthService authService, GameService gameService)
    {
        this.authService = authService;
        this.gameService = gameService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx)
    {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws IOException
    {
        try
        {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.commandType())
            {
                case CONNECT -> connect(ctx.session, command);
                case MAKE_MOVE -> makeMove(ctx.session);
            }
        } catch (IOException | DataAccessException ex)
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
        String username = authService.getUsernameFromToken(command.authToken());
        connectionManager.addToGame(session, command.gameID(), username);
        session.getRemote().sendString(serializer.toJson(new LoadGame(gameService.getById(command.gameID()))));
        connectionManager.broadcast(session, new Notification("User " + username + " connected"), command.gameID());
    }

    private void makeMove(Session session)
    {
        System.out.println("Connect called");
    }
}