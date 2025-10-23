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

    public void logout(Context context) {
        String token = context.header("authorization");
        if (token == null || token.isEmpty()) {
            setErrorContext(context, "401 Unauthorized Error: No token supplied", 401);
            return;
        }

        try {
            authService.logout(token);
            context.status(200); // Optional: indicate success
        } catch (UserNotValidatedException e) {
            // Invalid token
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        } catch (DataAccessException e) {
            // Internal error
            setErrorContext(context, "500 Error: An internal error occurred", 500);
        }
    }
}