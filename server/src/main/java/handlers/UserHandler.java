package handlers;

import com.google.gson.Gson;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidCredentialsException;
import dataaccess.exceptions.MissingFieldException;
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

    public void clear(Context ctx) throws DataAccessException, MissingFieldException
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
        } catch (MissingFieldException e) { setErrorContext(ctx, e.getMessage(), 400); }
        catch (AlreadyTakenException e) { setErrorContext(ctx, e.getMessage(), 403); }
        catch (DataAccessException e) { setErrorContext(ctx, "Internal Server Error", 500); }
    }

    public void login(Context ctx) {
        try {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            AuthResult result = userService.login(req.username(), req.password());
            ctx.result(buildJson("username", result.username(), "authToken", result.authToken()));
        } catch (MissingFieldException e) { setErrorContext(ctx, e.getMessage(), 400); }
        catch (InvalidCredentialsException e) { setErrorContext(ctx, e.getMessage(), 401); }
        catch (DataAccessException e) { setErrorContext(ctx, "Internal Server Error", 500); }
    }
}