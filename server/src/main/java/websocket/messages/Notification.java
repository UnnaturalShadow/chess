package websocket.messages;

import static websocket.messages.ServerMessage.ServerMessageType.NOTIFICATION;

public class Notification extends ServerMessage
{
    public String message;
    public Notification (String message)
    {
        super(NOTIFICATION);
        this.message = message;
    }
}