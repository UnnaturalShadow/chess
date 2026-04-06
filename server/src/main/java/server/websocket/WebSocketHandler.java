package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocketmessage.Action;
import websocketmessage.Notification;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            Action action = new Gson().fromJson(ctx.message(), Action.class);
            switch (action.type()) {
                case ENTER -> enter(action.visitorName(), ctx.session);
                case EXIT -> exit(action.visitorName(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(String playerName, Session session) throws IOException {
        connections.add(session);
        var message = String.format("%s has joined the game", playerName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(session, notification);
    }

    private void exit(String visitorName, Session session) throws IOException {
        var message = String.format("%s has left the game", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private void handleConnect(UserGameCommand cmd, Session session) throws IOException {
        connections.add(cmd.getGameID(), session);

        // 1. Send LOAD_GAME to root client
        ServerMessage load = new ServerMessage();
        load.serverMessageType = ServerMessage.ServerMessageType.LOAD_GAME;
        load.game = getGame(cmd.gameID); // you'll implement this

        session.getRemote().sendString(new Gson().toJson(load));

        // 2. Notify others
        ServerMessage notif = new ServerMessage();
        notif.serverMessageType = ServerMessage.ServerMessageType.NOTIFICATION;
        notif.message = "User connected to game";

        connections.broadcast(cmd.gameID, session, new Gson().toJson(notif));
    }
}