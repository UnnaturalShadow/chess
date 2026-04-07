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
import org.eclipse.jetty.websocket.api.Session;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.WebSocketManager;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;
import chess.ChessMove;

import java.util.HashMap;
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

        // WebSocket endpoint
        app.ws("/ws", ws -> {

            ws.onConnect(ctx ->
                    System.out.println("WebSocket connected: " + ctx.sessionId())
            );

            ws.onMessage(ctx ->
                    handleWebSocketMessage(ctx, ctx.message())
            );

            ws.onClose(ctx -> {
                System.out.println("WebSocket disconnected: " + ctx.sessionId());
                WebSocketManager.removeSession(ctx.session);
            });

            ws.onError(ctx ->
                    System.out.println("WebSocket error: " + ctx.error())
            );
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
    // WebSocket Logic
    // =========================

    private void handleWebSocketMessage(io.javalin.websocket.WsContext ctx, String messageJson) {
        try {
            UserGameCommand base = gson.fromJson(messageJson, UserGameCommand.class);

            switch (base.getCommandType()) {

                case CONNECT -> handleConnect(ctx, base);

                case MAKE_MOVE -> {
                    MakeMoveCommand moveCmd = gson.fromJson(messageJson, MakeMoveCommand.class);
                    handleMakeMove(ctx, moveCmd);
                }

                case LEAVE -> handleLeave(ctx, base);

                case RESIGN -> handleResign(ctx, base);
            }

        } catch (Exception e) {
            sendError(ctx, "Error: Invalid command");
        }
    }

    private void handleConnect(io.javalin.websocket.WsContext ctx, UserGameCommand cmd) {
        try {
            var username = authDAO.findUsernameByToken(cmd.getAuthToken());
            var gameData = gameDAO.findById(cmd.getGameID());

            if (username == null || gameData == null) {
                sendError(ctx, "Error: invalid auth or game");
                return;
            }

            WebSocketManager.addSession(cmd.getGameID(), ctx.session);

            // Send current game to root client
            ctx.send(gson.toJson(new LoadGameMessage(gameData.game())));

            // Notify others
            String message = username + " joined the game";
            broadcast(cmd.getGameID(), new NotificationMessage(message), ctx.session);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleMakeMove(io.javalin.websocket.WsContext ctx, MakeMoveCommand cmd) {
        try {
            var username = authDAO.findUsernameByToken(cmd.getAuthToken());
            if (username == null) {
                sendError(ctx, "Error: invalid auth");
                return;
            }

            ChessMove move = cmd.getMove();

            var updatedGame = gameService.makeMove(cmd.getAuthToken(), cmd.getGameID(), move);

            // Send updated game to ALL clients
            String loadJson = gson.toJson(new LoadGameMessage(updatedGame.game()));
            for (Session s : WebSocketManager.getSessions(cmd.getGameID())) {
                if (s.isOpen()) {
                    s.getRemote().sendString(loadJson);
                }
            }

            // Notify others
            String message = username + " made a move";
            broadcast(cmd.getGameID(), new NotificationMessage(message), ctx.session);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(io.javalin.websocket.WsContext ctx, UserGameCommand cmd) {
        try {
            var username = authDAO.findUsernameByToken(cmd.getAuthToken());

            WebSocketManager.removeSession(ctx.session);

            if (username != null) {
                String message = username + " left the game";
                broadcast(cmd.getGameID(), new NotificationMessage(message), ctx.session);
            }

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleResign(io.javalin.websocket.WsContext ctx, UserGameCommand cmd) {
//        try {
//            var username = authDAO.findUsernameByToken(cmd.getAuthToken());
//
//            gameService.resign(cmd.getAuthToken(), cmd.getGameID());
//
//            String message = username + " resigned the game";
//            String json = gson.toJson(new NotificationMessage(message));
//
//            for (Session s : WebSocketManager.getSessions(cmd.getGameID())) {
//                if (s.isOpen()) {
//                    s.getRemote().sendString(json);
//                }
//            }
//
//        } catch (Exception e) {
//            sendError(ctx, "Error: " + e.getMessage());
//        }
        return;
    }

    private void broadcast(int gameID, ServerMessage message, Session exclude) {
        String json = gson.toJson(message);

        for (Session s : WebSocketManager.getSessions(gameID)) {
            if (s.isOpen() && s != exclude) {
                try {
                    s.getRemote().sendString(json);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void sendError(io.javalin.websocket.WsContext ctx, String msg) {
        ctx.send(gson.toJson(new ErrorMessage(msg)));
    }

    // =========================
    // Utility Methods
    // =========================

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