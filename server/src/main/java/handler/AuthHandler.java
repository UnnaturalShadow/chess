package handler;

import dataaccess.exceptions.UserNotValidatedException;
import io.javalin.http.Context;
import service.AuthService;

import static server.Server.setErrorContext;

public class AuthHandler
{
    AuthService authService;
    public AuthHandler(AuthService authService)
    {
        this.authService = authService;
    }

    public void logout(Context context)
    {
        String token = context.header("authorization");

        try
        {
            authService.logout(token);
            context.status(200); // Success
        }
        catch (UserNotValidatedException e)
        {
            // Invalid or missing token → 401
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        }
    }
}