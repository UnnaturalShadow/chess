package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;
import service.GameService;

import java.util.List;
import java.util.Objects;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class GameHandler
{

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService)
    {
        this.gameService = Objects.requireNonNull(gameService);
    }

    public void list(Context ctx)
    {
        String token = ctx.header("authorization");
        try
        {
            List<GameData> games = gameService.list(token);
            ctx.result(buildJson("games", games));
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "401 Unauthorized Error: Invalid token", 401);
        }
    }

    public void create(Context ctx)
    {
        String token = ctx.header("authorization");
        try
        {
            CreateRequest req = gson.fromJson(ctx.body(), CreateRequest.class);
            int gameID = gameService.create(token, req);
            ctx.result(buildJson("gameID", gameID));
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "400 Error: Could not create game", 400);
        }
    }

    public void join(Context ctx)
    {
        String token = ctx.header("authorization");
        try
        {
            JoinRequest req = gson.fromJson(ctx.body(), JoinRequest.class);
            gameService.join(token, req);
            ctx.status(200);
        } catch (AlreadyTakenException e)
        {
            setErrorContext(ctx, "403 Conflict Error: Color already taken", 403);
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "400 Error: Could not join game", 400);
        }
    }
}