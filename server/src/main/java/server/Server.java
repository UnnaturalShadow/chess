package server;

import handler.AuthHandler;
import handler.GameHandler;
import handler.UserHandler;
import com.google.gson.Gson;
import dataaccess.DAOCollection;
import io.javalin.*;
import io.javalin.http.Context;
import service.AppService;
import service.AuthService;
import service.GameService;
import service.UserService;

import java.util.Map;

public class Server {
    private final Javalin javalin;
    DAOCollection DAOs = new DAOCollection();
    AuthService authService = new AuthService(DAOs);
    AuthHandler authHandler = new AuthHandler(authService);
    UserService userService = new UserService(DAOs);
    UserHandler userHandler = new UserHandler(userService);
    GameService gameService = new GameService(DAOs);
    GameHandler gameHandler = new GameHandler(gameService);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", new AppService(DAOs)::clear)
                .post("/user", userHandler::create)
                .post("/session", userHandler::login)
                .delete("/session", authHandler::logout)
                .post("/game", gameHandler::create)
                .get("/game", gameHandler::list)
                .put("/game", gameHandler::join);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
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
        context.result(buildJson("message", message));
        context.status(status);
    }
}