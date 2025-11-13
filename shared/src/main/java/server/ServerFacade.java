package server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import exception.ResponseException;
import model.*;
import requestobjects.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade
{
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url)
    {
        serverUrl = url;
    }

    public void clearDb() throws ResponseException
    {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public RegisterResult createUser(RegisterRequest registerRequest) throws ResponseException
    {
        var request = buildRequest("POST", "/user", registerRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public LoginResult loginUser(LoginRequest loginRequest) throws ResponseException
    {
        var request = buildRequest("POST", "/session", loginRequest, null);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public void logoutUser(String token) throws ResponseException
    {
        var request = buildRequest("DELETE", "/session", null, token);
        var response = sendRequest(request);
        handleResponse(response, LoginResult.class);
    }

    public CreateResult createGame(String token, CreateRequest createRequest) throws ResponseException
    {
        var request = buildRequest("POST", "/game", createRequest, token);
        var response = sendRequest(request);
        return handleResponse(response, CreateResult.class);
    }

    public ListResult listGame(String token) throws ResponseException
    {
        var request = buildRequest("GET", "/game", null, token);
        var response = sendRequest(request);
        return handleResponse(response, ListResult.class);
    }

    public void joinGame(String token, JoinRequest joinRequest) throws ResponseException
    {
        var request = buildRequest("PUT", "/game", joinRequest, token);
        var response = sendRequest(request);
        handleResponse(response, ListResult.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String token)
    {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null)
        {
            request.setHeader("Content-Type", "application/json");
        }

        if (token != null)
        {
            request.setHeader("authorization", token);
        }

        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request)
    {
        if (request != null)
        {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else
        {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException
    {
        try
        {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex)
        {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException
    {
        var status = response.statusCode();
        if (!isSuccessful(status))
        {
            var body = response.body();
            if (body != null)
            {
                throw new IllegalArgumentException(JsonParser.parseString(body)
                        .getAsJsonObject()
                        .get("message")
                        .getAsString());
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null)
        {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status)
    {
        return status / 100 == 2;
    }
}