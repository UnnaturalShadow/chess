package websocket;


import websocket.messages.LoadGame;
import websocket.messages.Notification;

public interface NotificationHandler
{
    void notify(Notification message);
    void loadGame(LoadGame message);
}