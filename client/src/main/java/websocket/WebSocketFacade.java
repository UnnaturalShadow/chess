package websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import org.glassfish.tyrus.core.wsadl.model.Endpoint;
import websocket.messages.Notification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint
{
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException
    {
        try
        {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>()
            {
                @Override
                public void onMessage(String message)
                {
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }
}