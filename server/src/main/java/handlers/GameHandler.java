package handlers;

import com.google.gson.Gson;
import dataaccess.exceptions.*;
import io.javalin.http.Context;
import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;
import service.GameService;

import java.util.List;
import java.util.Objects;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService);
    }

    public void list(Context ctx) {
        String token = ctx.header("authorization");

        try {
            List<GameData> games = gameService.list(token);
            ctx.status(200);
            ctx.result(buildJson("games", games));

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, "Error: User not found.", 401);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }

    public void create(Context ctx) {
        String token = ctx.header("authorization");
        CreateRequest req = gson.fromJson(ctx.body(), CreateRequest.class);

        try {
            int gameID = gameService.create(token, req);
            ctx.status(200);
            ctx.result(buildJson("gameID", gameID));

        } catch (MissingFieldException e) {
            setErrorContext(ctx, "Error: Game name is required", 400);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, "Error: User not found.", 401);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }

    public void join(Context ctx) {
        String token = ctx.header("authorization");
        JoinRequest req = gson.fromJson(ctx.body(), JoinRequest.class);

        try {
            gameService.join(token, req);
            ctx.status(200);

        } catch (AlreadyTakenException e) {
            setErrorContext(ctx, "Error: Color already taken", 403);

        } catch (GameNotFoundException e) {
            setErrorContext(ctx, "Error: Game not found", 400);

        } catch (MissingFieldException e) {
            setErrorContext(ctx, "Error: Join requires a game ID", 400);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, "Error: User not found.", 401);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }
}