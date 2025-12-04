package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import dataaccess.DataAccessException;
import service.AuthService;
import websocket.messages.Notification;
import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler
{

    ConnectionManager connectionManager = new ConnectionManager();
    AuthService authService;

    public WebSocketHandler(AuthService authService)
    {
        this.authService = authService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx)
    {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx)
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
            ex.printStackTrace();
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
        connectionManager.broadcast(null, new Notification("User " + username + " connected"), command.gameID());
    }

    private void makeMove(Session session)
    {
        System.out.println("Connect called");
    }
}