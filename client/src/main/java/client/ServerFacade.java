package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.time.Duration;

public class ServerFacade
{
    private static final int TIMEOUT_MS = 5000;

    private final String serverUrl;
    private final HttpClient client;
    private final Gson gson;

    public ServerFacade(int port)
    {
        this.serverUrl = "http://localhost:" + port;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // =========================
    // Public API Methods
    // =========================

    public AuthData register(String username, String password, String email) throws ResponseException
    {
        var body = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", body, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException
    {
        var body = new LoginRequest(username, password);
        return makeRequest("POST", "/session", body, null, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException
    {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException
    {
        var body = new CreateGameRequest(gameName);
        var response = makeRequest("POST", "/game", body, authToken, CreateGameResponse.class);
        return response.gameID();
    }

    public GameData[] listGames(String authToken) throws ResponseException
    {
        var response = makeRequest("GET", "/game", null, authToken, ListGamesResponse.class);
        return response.games();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws ResponseException
    {
        var body = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", body, authToken, null);
    }

    // =========================
    // Core HTTP Logic
    // =========================

    private <T> T makeRequest(String method,
                              String path,
                              Object requestBody,
                              String authToken,
                              Class<T> responseClass) throws ResponseException
    {
        try
        {
            URI uri = new URI(serverUrl + path);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMillis(TIMEOUT_MS))
                    .header("Content-Type", "application/json");

            if (authToken != null)
            {
                builder.header("Authorization", authToken);
            }

            // Attach body if needed
            if (requestBody != null)
            {
                String json = gson.toJson(requestBody);
                builder.method(method, HttpRequest.BodyPublishers.ofString(json));
            }
            else
            {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpRequest request = builder.build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status >= 200 && status < 300)
            {
                if (responseClass == null)
                {
                    return null;
                }
                return gson.fromJson(response.body(), responseClass);
            }
            else
            {
                handleError(response);
                return null; // unreachable
            }
        }
        catch (IOException | InterruptedException | URISyntaxException ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private void handleError(HttpResponse<String> response) throws ResponseException
    {
        try
        {
            ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);
            throw new ResponseException(ResponseException.Code.ServerError, error.message());
        }
        catch (Exception e)
        {
            throw new ResponseException(
                    ResponseException.Code.ServerError,
                    "HTTP " + response.statusCode() + ": " + response.body()
            );
        }
    }

    // =========================
    // Request/Response DTOs
    // =========================

    private record RegisterRequest(String username, String password, String email) {}
    private record LoginRequest(String username, String password) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(String playerColor, int gameID) {}

    private record CreateGameResponse(int gameID) {}
    private record ListGamesResponse(GameData[] games) {}
    private record ErrorResponse(String message) {}
}