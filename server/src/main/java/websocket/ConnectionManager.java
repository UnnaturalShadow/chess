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
    public Map<Session, Integer> sessions = new HashMap<>(); // Maps a session to the game id that it is in
    public Map<Integer, Set<Session>> games = new HashMap<>(); // Maps a set of sessions to a game id

    public void add(Session session, int gameId)
    {
        sessions.put(session, gameId);
        games.get(gameId).add(session);
    }

    public void remove(Session session)
    {
        games.get(sessions.get(session)).remove(session);
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