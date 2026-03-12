package server;

import com.google.gson.Gson;
import dataaccess.database.DatabaseAuthDAO;
import dataaccess.database.DatabaseGameDAO;
import dataaccess.database.DatabaseUserDAO;
import dataaccess.exceptions.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handlers.AuthHandler;
import handlers.GameHandler;
import handlers.UserHandler;
import io.javalin.Javalin;
import service.AuthService;
import service.GameService;
import service.UserService;

import dataaccess.exceptions.DataAccessException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server
{
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    private final Javalin app;

    // --- DAOs ---


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
        try
        {
            authDAO = new DatabaseAuthDAO();
            userDAO = new DatabaseUserDAO();
            gameDAO = new DatabaseGameDAO();
        }
        catch (DataAccessException e)
        {
            System.err.println("Error connecting to database.");
        }
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