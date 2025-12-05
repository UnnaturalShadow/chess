package server;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.database.DatabaseDaoCollection;
import handler.AuthHandler;
import handler.GameHandler;
import handler.UserHandler;
import com.google.gson.Gson;
import dataaccess.DaoCollection;
import io.javalin.*;
import io.javalin.http.Context;
import service.AppService;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.WebSocketHandler;

import java.util.Map;

public class Server {
    private final Javalin javalin;
    public DaoCollection daos = new DatabaseDaoCollection();
    AuthService authService = new AuthService(daos);
    AuthHandler authHandler = new AuthHandler(authService);
    UserService userService = new UserService(daos);
    UserHandler userHandler = new UserHandler(userService);
    GameService gameService = new GameService(daos);
    GameHandler gameHandler = new GameHandler(gameService);
    DatabaseManager databaseManager = new DatabaseManager();
    private final WebSocketHandler webSocketHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        webSocketHandler = new WebSocketHandler(authService, gameService, userService);

        javalin.delete("/db", new AppService(daos)::clear)
                .post("/user", userHandler::create)
                .post("/session", userHandler::login)
                .delete("/session", authHandler::logout)
                .post("/game", gameHandler::create)
                .get("/game", gameHandler::list)
                .put("/game", gameHandler::join)
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketHandler);
                    ws.onMessage(webSocketHandler);
                    ws.onClose(webSocketHandler);
                });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        try {
            databaseManager.configureDatabase();
        } catch (DataAccessException e) {
            System.out.println("Failed to set up the database");
        }

        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public static String buildJson(Object... keysAndVals) {
        Map<String, Object> pairs = new java.util.HashMap<>(Map.of());
        for (int i = 1; i < keysAndVals.length; i++){
            if (i%2 == 1) {
                pairs.put((String) keysAndVals[i-1], keysAndVals[i]);
            }
        }

        return new Gson().toJson(pairs);
    }

    public static void setErrorContext(Context context, String message, int status) {
        context.result(buildJson("message", message, "status", status));
        context.status(status);
    }
}