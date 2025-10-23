package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;
import io.javalin.http.Context;
import requestobjects.LoginRequest;
import requestobjects.LoginResult;
import requestobjects.RegisterRequest;
import requestobjects.RegisterResult;
import service.UserService;

import static server.Server.buildJson;
import static server.Server.setErrorContext;

public class UserHandler {
    UserService userService;
    Gson serializer = new Gson();
    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void clear(Context context) {
        userService.clear();
        context.result("{}");
    }

    public void create(Context context) {
        try {
            RegisterResult result = userService.register(serializer.fromJson(context.body(), RegisterRequest.class));
            context.result(buildJson("username", result.username(), "authToken", result.authToken()));
        } catch (AlreadyTakenException e) {
            setErrorContext(context, "403 Already Taken Error: Username already taken.", 403);
        } catch (DataAccessException e) {
            setErrorContext(context,"500 Data Access Error: Failed to create new user", 500);
        } catch (BadRequestException e) {
            setErrorContext(context,"400 Bad Request Error: Some field was missing", 400);
        }
    }

    public void login(Context context) {
        try {
            LoginResult result = userService.login(serializer.fromJson(context.body(), LoginRequest.class));
            context.result(buildJson("username", result.username(), "authToken", result.authToken()));
        } catch (BadRequestException e) {
            setErrorContext(context,"400 Bad Request Error: Some field was missing", 400);
        } catch (UserNotValidatedException e) {
            setErrorContext(context, "401 Unauthorized Error: User could not be logged in", 401);
        }
    }
}