package handlers;

import com.google.gson.Gson;
import dataaccess.exceptions.*;
import io.javalin.http.Context;
import requests.AuthResult;
import requests.LoginRequest;
import requests.RegisterRequest;
import service.UserService;

import java.util.Objects;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class UserHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }

    public void clear(Context ctx) {
        try {
            userService.clear();
            ctx.status(200);
            ctx.result(buildJson("message", "Database cleared successfully"));

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error: Unable to clear database", 500);
        }
    }

    public void register(Context ctx) {
        RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);

        try {
            AuthResult result = userService.register(
                    req.username(),
                    req.password(),
                    req.email()
            );

            ctx.status(200);
            ctx.result(buildJson(
                    "username", result.username(),
                    "authToken", result.authToken()
            ));

        } catch (AlreadyTakenException e) {
            setErrorContext(ctx, "Error: Username already in use", 403);

        } catch (MissingFieldException e) {
            setErrorContext(ctx, "Error: Username and password are required", 400);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }

    public void login(Context ctx) {
        LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);

        try {
            AuthResult result = userService.login(req.username(), req.password());

            ctx.status(200);
            ctx.result(buildJson(
                    "username", result.username(),
                    "authToken", result.authToken()
            ));

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, "Error: Invalid username or password", 401);

        } catch (UserNotAuthenticatedException e) {
            setErrorContext(ctx, "Error: User is not authenticated", 401);

        } catch (MissingFieldException e) {
            setErrorContext(ctx, "Error: Username and password are required", 400);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }
}