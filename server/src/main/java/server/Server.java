package server;

import com.google.gson.Gson;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handler.AuthHandler;
import handler.GameHandler;
import handler.UserHandler;
import io.javalin.Javalin;
import service.AuthService;
import service.GameService;
import service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server
{

    private final Javalin app;

    // --- DAOs ---
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final UserDAO userDAO = new MemoryUserDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    // --- Services ---
    private final AuthService authService = new AuthService(authDAO);
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);

    // --- Handlers ---
    private final AuthHandler authHandler = new AuthHandler(authService);
    private final UserHandler userHandler = new UserHandler(userService);
    private final GameHandler gameHandler = new GameHandler(gameService);

    public Server()
    {
        app = Javalin.create(config -> config.staticFiles.add("/web"));

        // --- Routes ---
        app.delete("/db", ctx ->
        {
            userDAO.clear();
            gameDAO.clear();
            authDAO.clear();
            ctx.status(200);
        });

        app.post("/user", userHandler::register);
        app.post("/session", userHandler::login);
        app.delete("/session", authHandler::logout);

        app.post("/game", gameHandler::create);
        app.get("/game", gameHandler::list);
        app.put("/game", gameHandler::join);
    }

    public int run(int desiredPort)
    {
        app.start(desiredPort);
        return app.port();
    }

    public void stop()
    {
        app.stop();
    }

    // --- Helper methods for JSON responses ---
    public static String buildJson(Object... keysAndVals)
    {
        Map<String, Object> pairs = new HashMap<>();
        for (int i = 1; i < keysAndVals.length; i++)
        {
            if (i % 2 == 1)
            {
                pairs.put((String) keysAndVals[i - 1], keysAndVals[i]);
            }
        }
        return new Gson().toJson(pairs);
    }

    public static void setErrorContext(io.javalin.http.Context ctx, String message, int status)
    {
        ctx.result(buildJson("message", message));
        ctx.status(status);
    }
}