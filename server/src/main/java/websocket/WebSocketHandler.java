package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler
{
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
                case CONNECT -> connect(ctx.session);
                case MAKE_MOVE -> makeMove(ctx.session);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx)
    {
        System.out.println("Websocket closed");
    }

    private void connect(Session session)
    {
        System.out.println("Connect called");
    }

    private void makeMove(Session session)
    {
        System.out.println("Connect called");
    }
}