package client.websocket;

import websocketmessage.Notification;

public interface NotificationHandler
{
    void notify(Notification notification);
}