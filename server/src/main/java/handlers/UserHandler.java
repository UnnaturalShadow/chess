package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import requests.AuthResult;
import requests.LoginRequest;
import requests.RegisterRequest;
import service.UserService;

import java.util.Objects;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class UserHandler
{

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService)
    {
        this.userService = Objects.requireNonNull(userService);
    }

    public void clear(Context ctx)
    {
        userService.clear();
        ctx.result("" +
                "{}");
    }

    public void register(Context ctx)
    {
        try
        {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            AuthResult result = userService.register(req.username(), req.password(), req.email());
            ctx.result(buildJson("username", result.username(), "authToken", result.authToken()));
        } catch (AlreadyTakenException e)
        {
            setErrorContext(ctx, "403 Already Taken Error: Username already in use", 403);
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "400 Data Access Error: Failed to register user", 400);
        }
    }

    public void login(Context ctx)
    {
        try
        {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            AuthResult result = userService.login(req.username(), req.password());
            ctx.result(buildJson("username", result.username(), "authToken", result.authToken()));
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "401 Unauthorized Error: Invalid username or password", 401);
        }
    }
}