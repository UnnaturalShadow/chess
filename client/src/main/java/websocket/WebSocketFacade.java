package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import exception.ResponseException;
import jakarta.websocket.*;
import jakarta.websocket.Endpoint;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint
{
    Session session;
    NotificationHandler notificationHandler;

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig)
    {
    }

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
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME)
                    {
                        serverMessage = new Gson().fromJson(message, LoadGame.class);
                    }

                    switch (serverMessage.getServerMessageType())
                    {
                        case LOAD_GAME -> notificationHandler.loadGame(new Gson().fromJson(message, LoadGame.class));
                        case NOTIFICATION -> notificationHandler.notify(new Gson().fromJson(message, Notification.class));
                        case ERROR -> notificationHandler.error(new Gson().fromJson(message, ErrorMessage.class));
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void sendCommand(UserGameCommand.CommandType type, String token, int gameID) throws ResponseException
    {
        try
        {
            var command = new UserGameCommand(type, token, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void sendMakeMoveCommand(String token, int gameID, ChessMove move) throws ResponseException
    {
        try
        {
            var command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, token, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }
}