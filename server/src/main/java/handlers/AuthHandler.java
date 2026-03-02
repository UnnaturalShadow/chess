package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.AuthService;

import java.util.Objects;

import static server.Server.setErrorContext;

public class AuthHandler
{

    private final AuthService authService;

    public AuthHandler(AuthService authService)
    {
        this.authService = Objects.requireNonNull(authService);
    }

    public void logout(Context ctx)
    {
        String token = ctx.header("authorization");
        try
        {
            authService.logout(token);
            ctx.status(200);
        } catch (DataAccessException e)
        {
            setErrorContext(ctx, "401 Unauthorized Error: Invalid or expired token", 401);
        }
    }
}