package handler;

import io.javalin.http.Context;
import com.google.gson.Gson;
import model.UserData;
import service.UserService;
import model.AuthData;

public class UserHandler
{

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService)
    {
        this.userService = userService;
    }

    // This method will handle POST /user
    public void registerUser(Context ctx)
    {
        try
        {
            // Javalin provides the Context automatically when the endpoint is called
            String body = ctx.body();
            UserData userData = gson.fromJson(body, UserData.class);

            AuthData result = userService.register(userData);
            ctx.result(new Gson().toJson(result));  // convert Java object → JSON string manually
            ctx.contentType("application/json");    // tell the client it’s JSON
            ctx.status(200);
        }
        catch (Exception e)
        {
            ctx.result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
            ctx.contentType("application/json");
            ctx.status(400);
        }
    }

    // Error response helper
    private record ErrorResponse(String message) {}
}
