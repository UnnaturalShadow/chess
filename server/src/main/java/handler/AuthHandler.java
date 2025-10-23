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
        try
        {
            authService.logout(context.header("authorization"));
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