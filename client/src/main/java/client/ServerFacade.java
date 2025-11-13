// client/src/main/java/client/ServerFacade.java
package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Small ServerFacade implementation for Phase 5.
 *
 * NOTE: the endpoints used here are the common ones but may differ
 * slightly in your starter-server. If tests fail because of endpoint
 * path differences, update the path strings below to match your server.
 *
 * Requires Jackson: add to client/pom.xml:
 * <dependency>
 *   <groupId>com.fasterxml.jackson.core</groupId>
 *   <artifactId>jackson-databind</artifactId>
 *   <version>2.15.2</version>
 * </dependency>
 */
public class ServerFacade {
    private final HttpClient http;
    private final ObjectMapper json;
    private final String baseUrl; // includes protocol and port, e.g. http://localhost:12345

    public record AuthData(String authToken, String username, long userId) {}
    public record GameSummary(long id, String name, List<String> players) {}

    public ServerFacade(int port) {
        this("http://localhost:" + port);
    }

    public ServerFacade(String baseUrl) {
        this.http = HttpClient.newHttpClient();
        this.json = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    // --- Helper methods ---
    private HttpResponse<String> postJson(String path, Object body, String authToken) throws Exception {
        var reqBody = json.writeValueAsString(body);
        var builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody));
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        var req = builder.build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String authToken) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    // --- Public API methods -- adjust endpoint paths if your server uses different routes ---

    /**
     * Register a user and return AuthData
     */
    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        var resp = postJson("/api/register", body, null); // <-- change path if server differs
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Register failed: " + resp.body());
        }
        var node = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
        String token = (String) node.get("authToken");
        Number userId = (Number) node.getOrDefault("userId", 0);
        return new AuthData(token, username, userId.longValue());
    }

    /**
     * Login user, return AuthData
     */
    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        var resp = postJson("/api/login", body, null);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Login failed: " + resp.body());
        }
        var node = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
        String token = (String) node.get("authToken");
        Number userId = (Number) node.getOrDefault("userId", 0);
        return new AuthData(token, username, userId.longValue());
    }

    /**
     * Logout (server invalidates token)
     */
    public void logout(String authToken) throws Exception {
        var resp = postJson("/api/logout", Map.of(), authToken);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Logout failed: " + resp.body());
        }
    }

    /**
     * Create a game (does not join)
     * returns the created game id
     */
    public long createGame(String authToken, String gameName) throws Exception {
        var body = Map.of("name", gameName);
        var resp = postJson("/api/games", body, authToken); // POST /api/games
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Create game failed: " + resp.body());
        }
        var node = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
        Number id = (Number) node.getOrDefault("id", node.get("gameId"));
        return id.longValue();
    }

    /**
     * List all games - returns list of GameSummary
     */
    public List<GameSummary> listGames(String authToken) throws Exception {
        var resp = get("/api/games", authToken);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("List games failed: " + resp.body());
        }
        // Expecting JSON array of { id, name, players: [ ... ] }
        var list = json.readValue(resp.body(), new TypeReference<List<Map<String,Object>>>() {});
        return list.stream()
                .map(m -> {
                    Number id = (Number) m.getOrDefault("id", m.get("gameId"));
                    String name = (String) m.getOrDefault("name", "unnamed");
                    @SuppressWarnings("unchecked")
                    List<String> players = (List<String>) m.getOrDefault("players", List.of());
                    return new GameSummary(id.longValue(), name, players);
                }).toList();
    }

    /**
     * Join a game as a color ("white" or "black")
     * returns the game id joined
     */
    public long joinGame(String authToken, long gameId, String color) throws Exception {
        var body = Map.of("color", color);
        var resp = postJson("/api/games/" + gameId + "/join", body, authToken);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Join game failed: " + resp.body());
        }
        var node = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
        Number id = (Number) node.getOrDefault("id", gameId);
        return id.longValue();
    }

    /**
     * Observe a game (register observer)
     */
    public long observeGame(String authToken, long gameId) throws Exception {
        var resp = postJson("/api/games/" + gameId + "/observe", Map.of(), authToken);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Observe game failed: " + resp.body());
        }
        var node = json.readValue(resp.body(), new TypeReference<Map<String,Object>>() {});
        Number id = (Number) node.getOrDefault("id", gameId);
        return id.longValue();
    }

    // Optional: for tests, clear database if server provides such endpoint.
    // If your server does not support this, remove or change the path.
    public void clearDatabaseForTesting() throws Exception {
        var resp = postJson("/api/testing/clear", Map.of(), null);
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Test clear failed: " + resp.body());
        }
    }
}
