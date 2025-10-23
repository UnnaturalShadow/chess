package server;

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

import java.util.Map;

public class Server {
    private final Javalin javalin;
    DaoCollection daos = new DaoCollection();
    AuthService authService = new AuthService(daos);
    AuthHandler authHandlers = new AuthHandler(authService);
    UserService userService = new UserService(daos);
    UserHandler userHandlers = new UserHandler(userService);
    GameService gameService = new GameService(daos);
    GameHandler gameHandlers = new GameHandler(gameService);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", new AppService(daos)::clear)
                .post("/user", userHandlers::create)
                .post("/session", userHandlers::login)
                .delete("/session", authHandlers::logout)
                .post("/game", gameHandlers::create)
                .get("/game", gameHandlers::list)
                .put("/game", gameHandlers::join);
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