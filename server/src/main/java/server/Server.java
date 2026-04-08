package server;

import com.google.gson.Gson;
import dataaccess.database.DatabaseAuthDAO;
import dataaccess.database.DatabaseGameDAO;
import dataaccess.database.DatabaseUserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.exceptions.DataAccessException;
import handlers.AuthHandler;
import handlers.GameHandler;
import handlers.UserHandler;
import io.javalin.Javalin;
import service.AuthService;
import service.GameService;
import service.UserService;
import server.websocket.WebSocketHandler;

import java.util.Map;

public class Server {

    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    private AuthHandler authHandler;
    private UserHandler userHandler;
    private GameHandler gameHandler;

    private final Javalin app;
    private final Gson gson = new Gson();

    public Server() {
        try {
            authDAO = new DatabaseAuthDAO();
            userDAO = new DatabaseUserDAO();
            gameDAO = new DatabaseGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error connecting to database", e);
        }

        authService = new AuthService(authDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        authHandler = new AuthHandler(authService);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);

        app = Javalin.create(config -> config.staticFiles.add("/web"));

        // REST endpoints
        app.delete("/db", ctx -> {
            try {
                userDAO.clear();
                gameDAO.clear();
                authDAO.clear();
                ctx.result(buildJson("message", "Database cleared successfully"));
                ctx.status(200);
            } catch (DataAccessException e) {
                setErrorContext(ctx, "Internal Server Error: Unable to clear database", 500);
            }
        });

        app.post("/user", userHandler::register);
        app.post("/session", userHandler::login);
        app.delete("/session", authHandler::logout);

        app.post("/game", gameHandler::create);
        app.get("/game", gameHandler::list);
        app.put("/game", gameHandler::join);

        // =========================
        // WebSocket registration
        // =========================
        WebSocketHandler wsHandler = new WebSocketHandler(authDAO, gameDAO, gameService);

        app.ws("/ws", ws -> {
            ws.onConnect(wsHandler::handleConnect);
            ws.onMessage(wsHandler::handleMessage);
            ws.onClose(wsHandler::handleClose);
            ws.onError(ctx -> {});
        });
    }

    public int run(int desiredPort) {
        app.start(desiredPort);
        return app.port();
    }

    public void stop() {
        app.stop();
    }

    // =========================
    // Utility Methods
    // =========================

    public static String buildJson(Object... keysAndVals) {
        Map<String, Object> pairs = new java.util.HashMap<>();
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
