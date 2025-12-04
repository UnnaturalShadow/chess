package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public record UserGameCommand(CommandType commandType, String authToken, Integer gameID)
{

    public enum CommandType
    {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UserGameCommand(CommandType type, String token, Integer id)))
        {
            return false;
        }
        return commandType() == type &&
                Objects.equals(authToken(), token) &&
                Objects.equals(gameID(), id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(commandType(), authToken(), gameID());
    }
}