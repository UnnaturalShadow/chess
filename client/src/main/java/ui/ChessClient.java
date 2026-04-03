package ui;

import java.net.URI;
import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import client.ServerFacade;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.WebSocketClientEndpoint;

public class ChessClient {
    private String loginState = "Logged Out";

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);
    private ChessGame game;
    private GameData currentGameData;
    private AuthData auth = null;
    private List<GameData> lastGameList = new ArrayList<>();
    private WebSocketClientEndpoint wsClient;
    private final Gson gson = new Gson();

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        while (true) {
            try {
                System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_BLUE + loginState
                        + EscapeSequences.RESET_TEXT_COLOR + ">>> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                if (auth == null) handlePrelogin(input);
                else handlePostlogin(input);
            } catch (Exception e) {
                System.out.println(extractErrorMessage(e.getMessage()));
            }
        }
    }

    // ==========================
    // Prelogin Commands
    // ==========================
    private void handlePrelogin(String input) throws ResponseException {
        String[] parts = input.split(" ");
        String cmd = parts[0].toLowerCase();
        switch (cmd) {
            case "help" -> help();
            case "quit" -> quit();
            case "login" -> login(parts);
            case "register" -> register(parts);
            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    private void login(String[] parts) throws ResponseException {
        if (parts.length < 3) {
            System.out.println("Usage: login <username> <password>");
            return;
        }
        auth = server.login(parts[1], parts[2]);
        System.out.println("Logged in as " + auth.username());
        loginState = "Logged In";
    }

    private void register(String[] parts) throws ResponseException {
        if (parts.length < 4) {
            System.out.println("Usage: register <username> <password> <email>");
            return;
        }
        auth = server.register(parts[1], parts[2], parts[3]);
        System.out.println("Registered and logged in as " + auth.username());
        loginState = "Logged In";
    }

    private void quit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }

    // ==========================
    // Postlogin Commands
    // ==========================
    private void handlePostlogin(String input) throws ResponseException {
        String[] parts = input.split(" ");
        String cmd = parts[0].toLowerCase();
        switch (cmd) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> createGame(input);
            case "list" -> listGames();
            case "join" -> joinGame(parts);
            case "observe" -> observeGame(parts);
            case "move" -> makeMove(parts);
            case "quit" -> quit();
            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    private void logout() throws ResponseException {
        server.logout(auth.authToken());
        auth = null;
        currentGameData = null;
        game = null;
        if (wsClient != null) wsClient.close();
        wsClient = null;
        System.out.println("Logged out.");
        loginState = "Logged Out";
    }

    private void createGame(String input) throws ResponseException {
        if (!input.contains(" ")) {
            System.out.println("Usage: create <gameName>");
            return;
        }
        String name = input.substring(input.indexOf(" ") + 1);
        int id = server.createGame(auth.authToken(), name);
        System.out.println("Created game " + name);
    }

    private void listGames() throws ResponseException {
        GameData[] games = server.listGames(auth.authToken());
        lastGameList = Arrays.asList(games);
        if (games.length == 0) {
            System.out.println("No games available.");
            return;
        }
        for (int i = 0; i < games.length; i++) {
            var g = games[i];
            System.out.printf("%d. %s (White: %s, Black: %s)%n",
                    i + 1, g.gameName(), g.whiteUsername(), g.blackUsername());
        }
    }

    // ==========================
    // Join / Observe Game
    // ==========================
    private void joinGame(String[] parts) throws ResponseException {
        if (parts.length < 3) {
            System.out.println("Usage: join <number> <WHITE|BLACK>");
            return;
        }
        try {
            int index = Integer.parseInt(parts[1]) - 1;
            String color = parts[2].toUpperCase();
            if (!validIndex(index)) return;

            currentGameData = lastGameList.get(index);
            game = currentGameData.game();

            server.joinGame(auth.authToken(), currentGameData.gameID(), color);

            openWebSocket(currentGameData.gameID());
            sendConnectMessage();

            System.out.println("Joined game as " + color);
            loginState = "In Game";
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
        }
    }

    private void observeGame(String[] parts) throws ResponseException {
        if (parts.length < 2) {
            System.out.println("Usage: observe <number>");
            return;
        }
        try {
            int index = Integer.parseInt(parts[1]) - 1;
            if (!validIndex(index)) return;

            currentGameData = lastGameList.get(index);
            game = currentGameData.game();

            openWebSocket(currentGameData.gameID());
            sendConnectMessage();

            System.out.println("Observing game");
            loginState = "Observing";
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
        }
    }

    // ==========================
    // WebSocket
    // ==========================
    private void openWebSocket(int gameID) {
        if (wsClient != null) return;
        try {
            wsClient = new WebSocketClientEndpoint(new URI("ws://localhost:8080/ws"));
            wsClient.addMessageHandler(this::handleServerMessage);
        } catch (Exception e) {
            System.out.println("Failed to open WebSocket: " + e.getMessage());
        }
    }

    private void sendConnectMessage() {
        if (wsClient == null || currentGameData == null) return;

        UserGameCommand connectCmd = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                auth.authToken(),
                currentGameData.gameID()
        );
        wsClient.send(gson.toJson(connectCmd));
    }

    private void handleServerMessage(String messageJson) {
        try {
            ServerMessage msg = gson.fromJson(messageJson, ServerMessage.class);
            switch (msg.getServerMessageType()) {
                case LOAD_GAME -> {
                    currentGameData = gson.fromJson(messageJson, GameData.class);
                    game = currentGameData.game();
                    drawBoard(false);
                    System.out.println("Board updated.");
                }
                case NOTIFICATION -> {
                    JsonObject obj = JsonParser.parseString(messageJson).getAsJsonObject();
                    System.out.println("Notification: " + obj.get("message").getAsString());
                }
                case ERROR -> {
                    JsonObject obj = JsonParser.parseString(messageJson).getAsJsonObject();
                    System.out.println("Error: " + obj.get("errorMessage").getAsString());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse server message: " + e.getMessage());
        }
    }

    // ==========================
    // Make Move
    // ==========================
    private void makeMove(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: move <from><to> (e.g., e2e4)");
            return;
        }
        if (currentGameData == null || wsClient == null) {
            System.out.println("You are not in a game.");
            return;
        }

        String moveStr = parts[1];
        try {
            UserGameCommand moveCmd = new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    auth.authToken(),
                    currentGameData.gameID(),
                    moveStr
            );
            wsClient.send(gson.toJson(moveCmd));
        } catch (Exception e) {
            System.out.println("Failed to send move: " + e.getMessage());
        }
    }

    // ==========================
    // Utilities
    // ==========================
    private boolean validIndex(int index) {
        if (index < 0 || index >= lastGameList.size()) {
            System.out.println("Invalid game number.");
            return false;
        }
        return true;
    }

    private void help() {
        if (auth == null) {
            System.out.println("""
                    Commands:
                      help
                      login <username> <password>
                      register <username> <password> <email>
                      quit
                    """);
        } else {
            System.out.println("""
                    Commands:
                      help
                      create <gameName>
                      list
                      join <number> <WHITE|BLACK>
                      observe <number>
                      move <from><to>
                      logout
                      quit
                    """);
        }
    }

    private void drawBoard(boolean blackPerspective) {
        ChessBoardPrinter printer = new ChessBoardPrinter(game);
        printer.printBoard(blackPerspective);
    }

    public static String extractErrorMessage(String rawMessage) {
        try {
            int jsonStart = rawMessage.indexOf('{');
            if (jsonStart == -1) return rawMessage;

            String jsonPart = rawMessage.substring(jsonStart);
            JsonObject obj = JsonParser.parseString(jsonPart).getAsJsonObject();
            String message = obj.get("message").getAsString();
            if (message.startsWith("Error: ")) message = message.substring(7);
            return message;
        } catch (Exception e) {
            return rawMessage;
        }
    }
}