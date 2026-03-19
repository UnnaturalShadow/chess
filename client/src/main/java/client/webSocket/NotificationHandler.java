package client.webSocket;

import webSocketMessage.Notification;

public interface NotificationHandler
{
    void notify(Notification notification);
}