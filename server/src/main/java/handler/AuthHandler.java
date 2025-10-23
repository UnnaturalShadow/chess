package handler;

import dataaccess.DataAccessException;
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
        String authHeader = context.header("authorization");

        if (authHeader == null || authHeader.isEmpty())
        {
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
            return;
        }

        try
        {
            authService.logout(authHeader);
        }
        catch (DataAccessException e)
        {
            setErrorContext(context, "500 Error: An internal error occurred", 500);
        }
        catch (UserNotValidatedException e)
        {
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        }
    }
}