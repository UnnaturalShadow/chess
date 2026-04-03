package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods unless necessary for new features like moves.
 */
public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;

    // NEW: optional move string (e.g., "e2e4" or "e7e8Q")
    private final String move;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this(commandType, authToken, gameID, null);
    }

    // NEW constructor that includes a move
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, String move) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    // NEW getter for the move string
    public String getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGameCommand that)) return false;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID()) &&
                Objects.equals(getMove(), that.getMove());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getMove());
    }
}