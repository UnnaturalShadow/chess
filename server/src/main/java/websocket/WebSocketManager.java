package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.util.*;

public class WebSocketManager {

    // gameID -> sessions
    private static final Map<Integer, Set<Session>> gameSessions = new HashMap<>();

    // session -> gameID
    private static final Map<Session, Integer> sessionToGame = new HashMap<>();

    public static void addSession(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new HashSet<>()).add(session);
        sessionToGame.put(session, gameID);
    }

    public static void removeSession(Session session) {
        Integer gameID = sessionToGame.remove(session);
        if (gameID != null) {
            Set<Session> sessions = gameSessions.get(gameID);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameID);
                }
            }
        }
    }

    public static Set<Session> getSessions(int gameID) {
        return gameSessions.getOrDefault(gameID, Collections.emptySet());
    }
}