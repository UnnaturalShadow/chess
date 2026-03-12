package server;

import com.google.gson.Gson;
import dataaccess.database.DatabaseAuthDAO;
import dataaccess.database.DatabaseGameDAO;
import dataaccess.database.DatabaseUserDAO;
import dataaccess.exceptions.DataAccessException;
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

import java.util.HashMap;
import java.util.Map;

public class Server {

    // --- DAOs ---
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    // --- Services ---
    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    // --- Handlers ---
    private AuthHandler authHandler;
    private UserHandler userHandler;
    private GameHandler gameHandler;

    private final Javalin app;

    public Server() {

        try {
            authDAO = new DatabaseAuthDAO();
            userDAO = new DatabaseUserDAO();
            gameDAO = new DatabaseGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error connecting to database", e);
        }

        // Create services AFTER DAOs exist
        authService = new AuthService(authDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        // Create handlers AFTER services exist
        authHandler = new AuthHandler(authService);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);

        app = Javalin.create(config -> config.staticFiles.add("/web"));

        // --- Routes ---
        app.delete("/db", ctx -> {
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

    public int run(int desiredPort) {
        app.start(desiredPort);
        return app.port();
    }

    public void stop() {
        app.stop();
    }

    // --- Helper methods for JSON responses ---
    public static String buildJson(Object... keysAndVals) {
        Map<String, Object> pairs = new HashMap<>();
        for (int i = 1; i < keysAndVals.length; i++) {
            if (i % 2 == 1) {
                pairs.put((String) keysAndVals[i - 1], keysAndVals[i]);
            }
        }
        return new Gson().toJson(pairs);
    }

    public static void setErrorContext(io.javalin.http.Context ctx, String message, int status) {
        ctx.result(buildJson("message", message));
        ctx.status(status);
    }
}