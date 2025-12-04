package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConnectionManager
{
    public Map<Session, String> sessions = new HashMap<>(); // Maps a session to the username of the user that session belongs to
    public Map<Session, Integer> sessionsToGames = new HashMap<>(); // Maps a session to the game id that it is in
    public Map<Integer, Set<Session>> games = new HashMap<>(); // Maps a set of sessions to a game id

    public void addSession(Session session, String userName)
    {
        sessions.put(session, userName);
    }

    public void addToGame(Session session, int gameId, String userName)
    {
        sessionsToGames.put(session, gameId);
        sessions.put(session, userName);

        if (!games.containsKey(gameId))
        {
            games.put(gameId, Set.of(session));
        } else
        {
            games.get(gameId).add(session);
        }
    }

    public void remove(Session session)
    {
        games.get(sessionsToGames.get(session)).remove(session);
        sessionsToGames.remove(session);
        sessions.remove(session);
    }
    public void broadcast(Session excludeSession, ServerMessage message, int gameId) throws IOException
    {
        Gson serializer = new Gson();
        for (Session c : games.get(gameId))
        {
            if (c.isOpen())
            {
                if (!c.equals(excludeSession))
                {
                    c.getRemote().sendString(serializer.toJson(message));
                }
            }
        }
    }
}