package ui;

import java.net.URI;
import java.util.*;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import client.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

public class ChessClient {

    private String loginState = "Logged Out";
    private boolean inGame = false;

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);

    private ChessGame game;
    private GameData currentGameData;
    private AuthData auth = null;
    private List<GameData> lastGameList = new ArrayList<>();

    private WebSocketFacade ws;

    // Track player perspective (true = black at bottom)
    private boolean blackPerspective = false;
    private boolean observerMode = false;

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        while (true) {
            try {
                System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_BLUE + getPromptState()
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

    private String getPromptState() {
        if (inGame) {
            return "In Game";
        }
        return (auth != null) ? "Logged In" : "Logged Out";
    }

    // ==========================
    // Prelogin
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
    // Postlogin
    // ==========================
    private void handlePostlogin(String input) throws ResponseException {
        String[] parts = input.split(" ");
        String cmd = parts[0].toLowerCase();

        if (currentGameData != null) {
            // In-game commands
            switch (cmd) {
                case "help" -> help();
                case "redraw" -> drawBoard(blackPerspective);
                case "leave" -> leaveGame();
                case "move" -> makeMove(parts);
                case "highlight" -> highlightMoves(parts);
                case "resign" -> resign();
                case "quit" -> quit();
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        } else {
            // Post-login commands
            switch (cmd) {
                case "help" -> help();
                case "logout" -> logout();
                case "create" -> createGame(input);
                case "list" -> listGames();
                case "join" -> joinGame(parts);
                case "observe" -> observeGame(parts);
                case "quit" -> quit();
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void logout() throws ResponseException {
        server.logout(auth.authToken());
        auth = null;
        currentGameData = null;
        game = null;
        ws = null;
        blackPerspective = false;
        observerMode = false;
        System.out.println("Logged out.");
        loginState = "Logged Out";
    }

    private void createGame(String input) throws ResponseException {
        if (!input.contains(" ")) {
            System.out.println("Usage: create <gameName>");
            return;
        }
        String name = input.substring(input.indexOf(" ") + 1);
        server.createGame(auth.authToken(), name);
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
    // Join / Observe
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

            GameData selectedGame = lastGameList.get(index);
            server.joinGame(auth.authToken(), selectedGame.gameID(), color);

            blackPerspective = color.equals("BLACK");
            openWebSocket(selectedGame.gameID());
            ws.connect(auth.authToken(), selectedGame.gameID());

            currentGameData = selectedGame;
            game = currentGameData.game();
            observerMode = false;
            System.out.println("Joined game as " + color);
            inGame = true;

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

            GameData selectedGame = lastGameList.get(index);

            blackPerspective = false;
            openWebSocket(selectedGame.gameID());
            ws.connect(auth.authToken(), selectedGame.gameID());

            currentGameData = selectedGame;
            game = currentGameData.game();
            observerMode = true;
            inGame = true;
            System.out.println("Observing game");

        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
        }
    }

    // ==========================
    // WebSocket
    // ==========================
    private void openWebSocket(int gameID) {
        if (ws != null) return;

        try {
            ws = new WebSocketFacade(server.getServerUrl(), new NotificationHandler() {

                @Override
                public void loadGame(ChessGame gameFromServer) {
                    game = gameFromServer;
                    if (currentGameData != null) {
                        currentGameData = new GameData(
                                currentGameData.gameID(),
                                currentGameData.whiteUsername(),
                                currentGameData.blackUsername(),
                                currentGameData.gameName(),
                                gameFromServer,
                                currentGameData.gameOver()
                        );
                    }
                    drawBoard(blackPerspective);
                }

                @Override
                public void notify(String message) {
                    System.out.println(message);
                }

                @Override
                public void error(String message) {
                    System.out.println(message);
                }
            });

        } catch (Exception e) {
            System.out.println("Failed to open WebSocket: " + e.getMessage());
        }
    }

    // ==========================
    // Make Move
    // ==========================
    private void makeMove(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: move <from><to> or move <from> <to>");
            return;
        }

        if (currentGameData == null || ws == null) {
            System.out.println("You are not in a game.");
            return;
        }

        if (observerMode) {
            System.out.println("Observers cannot make moves.");
            return;
        }

        try {
            String moveStr;
            if (parts.length == 2) {
                moveStr = parts[1];
            } else if (parts.length == 3) {
                moveStr = parts[1] + parts[2];
            } else {
                System.out.println("Usage: move <from><to> or move <from> <to>");
                return;
            }

            ChessMove move = ChessMove.fromString(moveStr);
            ws.makeMove(auth.authToken(), currentGameData.gameID(), move);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid move format.");
        } catch (ResponseException e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    // ==========================
    // Highlight Moves
    // ==========================
    private void highlightMoves(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight <position>");
            return;
        }

        if (game == null) {
            System.out.println("No game loaded.");
            return;
        }

        try {
            ChessPosition pos = ChessPosition.fromAlgebraic(parts[1]);
            var moves = game.validMoves(pos);

            if (moves == null || moves.isEmpty()) {
                System.out.println("No valid moves.");
                drawBoard(blackPerspective);
                return;
            }

            Set<ChessPosition> highlights = new HashSet<>();
            for (ChessMove m : moves) {
                highlights.add(m.getEndPosition());
            }

            drawBoardWithHighlights(highlights);

        } catch (Exception e) {
            System.out.println("Invalid position.");
        }
    }

    // ==========================
    // Leave / Resign
    // ==========================
    private void leaveGame() throws ResponseException {
        if (ws != null && currentGameData != null) {
            ws.leave(auth.authToken(), currentGameData.gameID());
        }
        currentGameData = null;
        game = null;
        ws = null;
        blackPerspective = false;
        observerMode = false;
        System.out.println("Left the game.");
        inGame = false;
    }

    private void resign() throws ResponseException {
        if (ws != null && currentGameData != null) {
            if (observerMode) {
                System.out.println("Observers cannot resign.");
                return;
            }
            System.out.print("Confirm resign? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("yes")) {
                ws.resign(auth.authToken(), currentGameData.gameID());
                System.out.println("You have resigned.");
            } else {
                System.out.println("Resign cancelled.");
            }
        } else {
            System.out.println("You are not in a game.");
        }
    }

    // ==========================
    // Drawing
    // ==========================
    private void drawBoard(boolean blackPerspective) {
        ChessBoardPrinter printer = new ChessBoardPrinter(game);
        printer.printBoard(blackPerspective);
    }

    private void drawBoardWithHighlights(Set<ChessPosition> highlights) {
        ChessBoardPrinter printer = new ChessBoardPrinter(game);
        printer.printBoardWithHighlights(blackPerspective, highlights);
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
        if (currentGameData != null) {
            if (observerMode) {
                System.out.println("""
                        Gameplay Commands:
                          help                - Display this help text
                          redraw              - Redraw the chess board
                          leave               - Leave the current game
                          highlight <square>  - Highlight legal moves for a piece
                        """);
            } else {
                System.out.println("""
                        Gameplay Commands:
                          help                - Display this help text
                          redraw              - Redraw the chess board
                          leave               - Leave the current game
                          move <from><to>     - Make a move
                          resign              - Resign the game
                          highlight <square>  - Highlight legal moves for a piece
                        """);
            }
        } else {
            System.out.println("""
                    Post-Login Commands:
                      help
                      create <gameName>
                      list
                      join <number> <WHITE|BLACK>
                      observe <number>
                      logout
                      quit
                    """);
        }
    }

    public static String extractErrorMessage(String rawMessage) {
        try {
            int jsonStart = rawMessage.indexOf('{');
            if (jsonStart == -1) return rawMessage;

            String jsonPart = rawMessage.substring(jsonStart);
            JsonObject obj = JsonParser.parseString(jsonPart).getAsJsonObject();
            String message = obj.get("message").getAsString();

            if (message.startsWith("Error: ")) {
                message = message.substring(7);
            }

            return message;
        } catch (Exception e) {
            return rawMessage;
        }
    }
}
