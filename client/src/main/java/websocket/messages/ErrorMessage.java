package websocket.messages;

public class ErrorMessage extends ServerMessage
{
    public String errorMessage;

    public ErrorMessage(String error)
    {
        super(ServerMessageType.ERROR);
        this.errorMessage = error;
    }
}