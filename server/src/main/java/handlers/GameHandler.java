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

    // -------------------------------------------------------
    // LIST
    // -------------------------------------------------------

    public void list(Context ctx) throws DataAccessException{

        String token = ctx.header("authorization");

        try {
            List<GameData> games = gameService.list(token);
            ctx.result(buildJson("games", games));
            ctx.status(200);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, e.getMessage(), 401);
        }
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public void create(Context ctx) {

        String token = ctx.header("authorization");

        try {
            CreateRequest req = gson.fromJson(ctx.body(), CreateRequest.class);
            int gameID = gameService.create(token, req);

            ctx.result(buildJson("gameID", gameID));
            ctx.status(200);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, e.getMessage(), 401);

        } catch (MissingFieldException e) {
            setErrorContext(ctx, e.getMessage(), 400);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }

    // -------------------------------------------------------
    // JOIN
    // -------------------------------------------------------

    public void join(Context ctx) {

        String token = ctx.header("authorization");

        try {
            JoinRequest req = gson.fromJson(ctx.body(), JoinRequest.class);
            gameService.join(token, req);

            ctx.status(200);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, e.getMessage(), 401);

        } catch (MissingFieldException e) {
            setErrorContext(ctx, e.getMessage(), 400);

        } catch (GameNotFoundException e) {
            setErrorContext(ctx, e.getMessage(), 400);

        } catch (AlreadyTakenException e) {
            setErrorContext(ctx, e.getMessage(), 403);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }
}