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
import websocket.messages.ServerMessage;

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
    private final Gson gson = new Gson();

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

        // --- REST Routes ---
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

        // --- WebSocket Endpoint ---
        app.ws("/ws", ws -> {

            ws.onConnect(ctx -> {
                System.out.println("WebSocket connected: " + ctx.sessionId());
            });

            ws.onMessage(ctx -> {
                handleWebSocketMessage(ctx, ctx.message());
            });

            ws.onClose(ctx -> {
                System.out.println("WebSocket disconnected: " + ctx.sessionId());
                WebSocketManager.removeSession(ctx.session);
            });

            ws.onError(ctx -> {
                System.out.println("WebSocket error: " + ctx.error());
            });
        });
    }

    public int run(int desiredPort) {
        app.start(desiredPort);
        return app.port();
    }

    public void stop() {
        app.stop();
    }

    // --- WebSocket Logic ---

    private void handleWebSocketMessage(io.javalin.websocket.WsContext ctx, String messageJson) {
        try {
            UserGameCommand command = gson.fromJson(messageJson, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> {
                    // TODO (next step)
                }
                case LEAVE -> {
                    // TODO
                }
                case RESIGN -> {
                    // TODO
                }
            }

        } catch (Exception e) {
            sendError(ctx, "Error: Invalid command");
        }
    }

    private void handleConnect(io.javalin.websocket.WsContext ctx, UserGameCommand cmd) {
        try {
            var auth = authDAO.findUsernameByToken(cmd.getAuthToken());
            var game = gameDAO.findById(cmd.getGameID());

            if (auth == null || game == null) {
                sendError(ctx, "Error: invalid auth or game");
                return;
            }

            // Track session
            WebSocketManager.addSession(cmd.getGameID(), ctx.session);

            // Send LOAD_GAME to this client
            ServerMessage loadMsg = ServerMessage.loadGame(game);
            ctx.send(gson.toJson(loadMsg));

            // Notify others
            String username = auth.username;
            String message = username + " joined the game";

            broadcast(cmd.getGameID(), ServerMessage.notification(message), ctx.session);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
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
        ServerMessage error = ServerMessage.error(msg);
        ctx.send(gson.toJson(error));
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