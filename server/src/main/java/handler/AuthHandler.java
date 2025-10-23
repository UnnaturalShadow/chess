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

        try {
            authService.logout(token);
            context.status(200); // Success
        } catch (UserNotValidatedException e) {
            // Invalid or missing token → 401
            setErrorContext(context, "401 Unauthorized Error: Unauthorized", 401);
        } catch (Exception e) {
            // Unexpected errors → 500
            setErrorContext(context, "500 Error: An internal error occurred", 500);
        }
    }
}