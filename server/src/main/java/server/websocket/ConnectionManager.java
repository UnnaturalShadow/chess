package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Set<Session>> gameConnections = new ConcurrentHashMap<>();

    public void add(Integer gameID, Session session) {
        var sessions = gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet());
        sessions.add(session);
    }

    public void remove(Integer gameID, Session session) {
        var set = gameConnections.get(gameID);
        if (set != null) {
            set.remove(session);
        }
    }

    public void broadcast(Integer gameID, Session exclude, String message) throws IOException {
        var sessions = gameConnections.get(gameID);
        if (sessions == null) return;

        for (Session s : Set.copyOf(sessions)) {
            if (s.isOpen() && !s.equals(exclude)) {
                s.getRemote().sendString(message);
            }
        }
    }

    public void broadcastAll(Integer gameID, String message) throws IOException {
        var sessions = gameConnections.get(gameID);
        if (sessions == null) return;

        for (Session s : Set.copyOf(sessions)) {
            if (s.isOpen()) {
                s.getRemote().sendString(message);
            }
        }
    }
}
