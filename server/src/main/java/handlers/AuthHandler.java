package handlers;

import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidCredentialsException;
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

    public void logout(Context ctx) {
        String token = ctx.header("authorization");
        try {
            authService.logout(token);
            ctx.status(200);

        } catch (InvalidCredentialsException e) {
            setErrorContext(ctx, e.getMessage(), 401);

        } catch (DataAccessException e) {
            setErrorContext(ctx, "Internal Server Error", 500);
        }
    }
}