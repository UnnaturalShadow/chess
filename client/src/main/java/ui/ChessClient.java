package ui;

import static ui.EscapeSequences.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import model.GameData;
import websocket.commands.UserGameCommand;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
import requestobjects.LoginRequest;
import requestobjects.RegisterRequest;
import server.ServerFacade;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;

import java.util.*;

public class ChessClient implements NotificationHandler
{
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private final State state = new State();
    Scanner scanner = new Scanner(System.in);
    String authToken;

    public ChessClient(String url) throws ResponseException
    {
        server = new ServerFacade(url);
        ws = new WebSocketFacade(url, this);
    }

    public void loadGame(LoadGame message)
    {
        state.currentGame = message.game;
        printBoard(null);
        printPrompt();
    }

    public void notify(Notification message)
    {
        System.out.println(SET_TEXT_COLOR_BLUE + message.message);
        printPrompt();
    }

    public void error(ErrorMessage message)
    {
        System.out.println(SET_TEXT_COLOR_RED + message.errorMessage);
    }

    public void run()
    {
        System.out.println("Welcome to the chess client. Type \"help\" to see available actions.");

        var result = "";
        while (!result.equals("quit"))
        {
            printPrompt();
            String line = scanner.nextLine().toLowerCase();
            try
            {
                if (line.equals("help"))
                {
                    result = help();
                } else
                {
                    if (state.loggedInState == State.LoggedInState.SIGNEDOUT)
                    {
                        result = signedOutEval(line);
                    } else if (state.loggedInState == State.LoggedInState.SIGNEDIN)
                    {
                        result = signedInEval(line);
                    } else if (state.loggedInState == State.LoggedInState.INGAME)
                    {
                        result = inGameEval(line);
                    } else
                    {
                        result = "Unknown command. Type \"help\" to see valid commands.";
                    }
                }
                System.out.println(SET_TEXT_COLOR_BLUE + result);
            }
            catch (Throwable e)
            {
                var msg = e.getMessage().replaceFirst(".*Error: ", "");
                System.out.println(msg);
            }
        }
    }

    private String signedOutEval(String line)
    {
        return switch (line)
        {
            case "help" -> help();
            case "login" -> login();
            case "quit" -> "quit";
            case "register" -> register();
            default -> throw new IllegalStateException("Unexpected value: " + line + " Type help for valid commands. ");
        };
    }

    private String signedInEval(String line)
    {
        return switch (line)
        {
            case "help" -> help();
            case "logout" -> logout();
            case "create game" -> newGame();
            case "list games" -> listGames();
            case "play game" -> playGame();
            case "observe game" -> observeGame();
            default -> throw new IllegalStateException("Unexpected value: " + line + "Type help for valid commands.");
        };
    }
    private String inGameEval(String line)
    {
        return switch (line)
        {
            case "help" -> help();
            case "redraw" -> printBoard(null);
            case "leave" -> leave();
            case "resign" -> resign();
            case "make move" -> makeMove();
            case "highlight moves" -> highlightMoves();
            default -> throw new IllegalStateException("Unexpected value: " + line + "Type help for valid commands.");
        };
    }

    private void printPrompt()
    {
        System.out.print(RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_BLUE);
    }

    private String register()
    {
        System.out.println("Enter username");
        printPrompt();
        String username = scanner.nextLine();
        System.out.println("Enter password");
        printPrompt();
        String password = scanner.nextLine();
        System.out.println("Enter email");
        printPrompt();
        String email = scanner.nextLine();

        try
        {
            authToken = server.createUser(new RegisterRequest(username, password, email)).authToken();
            state.loggedInState = State.LoggedInState.SIGNEDIN;
            return "Successfully registered.";
        }
        catch (ResponseException e)
        {
            return "Could not complete registration.";
        }
    }

    private String login()
    {
        System.out.println("Enter username");
        printPrompt();
        String username = scanner.nextLine();
        System.out.println("Enter password");
        printPrompt();
        String password = scanner.nextLine();

        try
        {
            authToken = server.loginUser(new LoginRequest(username, password)).authToken();
            state.loggedInState = State.LoggedInState.SIGNEDIN;
            state.username = username;
            return "Successfully signed in.";
        }
        catch (ResponseException e)
        {
            return "Failed to sign in with the given credentials.";
        }
    }

    private String logout()
    {
        try
        {
            server.logoutUser(authToken);
            state.loggedInState = State.LoggedInState.SIGNEDOUT;
            return "Logging out...";
        }
        catch (ResponseException e)
        {
            return "Something failed while logging out. Check your connection to the server.";
        }
    }

    private String newGame()
    {
        System.out.println("Enter name for new game");
        printPrompt();
        String name = scanner.nextLine();
        try
        {
            server.createGame(authToken, new CreateRequest(name));
            state.games = server.listGame(authToken).games();
            return "Created game \"" + name + "\"";
        }
        catch (ResponseException e)
        {
            return "Failed to create new game";
        }
    }
    private String listGames()
    {
        try
        {
            state.games = server.listGame(authToken).games();
            StringBuilder gameStrings = new StringBuilder();
            gameStrings.append("Games\n");
            for (int i = 0; i < state.games.size(); i++)
            {
                GameData game = state.games.get(i);
                String gameString = (i+1) + ": " + game.gameName();
                if (game.whiteUsername() != null)
                {gameString += " White: " + game.whiteUsername();}
                if (game.blackUsername() != null)
                {gameString += " Black: " + game.blackUsername();}
                gameStrings.append(gameString).append("\n");
            }

            gameStrings.setLength(gameStrings.length() - 1);
            return gameStrings.toString();

        }
        catch (ResponseException e)
        {
            return "Failed to get games. Check your connection to the server and try again.";
        }
    }
    private String playGame()
    {
        GameData gameData = getGameFromUser();
        System.out.println("Which color would you like to join? (W)hite or (B)lack?");
        printPrompt();
        String line = scanner.nextLine();

        String color;
        if (line.equalsIgnoreCase("w"))
        {
            state.perspective = ChessGame.TeamColor.WHITE;
            color = "WHITE";
        } else if (line.equalsIgnoreCase("b"))
        {
            state.perspective = ChessGame.TeamColor.BLACK;
            color = "BLACK";
        } else
        {
            throw new IllegalArgumentException("Can't parse input. Accepted inputs are \"W\" or \"B\"");
        }
        try
        {
            server.joinGame(authToken, new JoinRequest(color, gameData.gameID()));
            state.games = server.listGame(authToken).games();
            ws.sendCommand(UserGameCommand.CommandType.CONNECT, authToken, gameData.gameID());
        }
        catch (Exception e)
        {
            return "Could not join game. "+ e.getMessage().replaceFirst(".*Error: ", "");
        }
        state.currentGame = gameData.game();
        state.loggedInState = State.LoggedInState.INGAME;
        state.currentGameId = gameData.gameID();

        return "";
    }
    private String observeGame()
    {
        try
        {
            GameData gameData = getGameFromUser();
            state.currentGame = gameData.game();
            state.perspective = ChessGame.TeamColor.WHITE;
            state.currentGameId = gameData.gameID();
            ws.sendCommand(UserGameCommand.CommandType.CONNECT, authToken, gameData.gameID());
            state.loggedInState = State.LoggedInState.INGAME;
            return printBoard(null);
        }
        catch (Exception e)
        {
            return "Could not join game. "+ e.getMessage().replaceFirst(".*Error: ", "");
        }
    }
    private GameData getGameFromUser()
    {
        try
        {
            if (state.games == null)
            {
                state.games = server.listGame(authToken).games();
            }

            System.out.println("Enter game id");
            printPrompt();
            String line = scanner.nextLine();
            return state.games.get(Integer.parseInt(line) - 1);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not find game with given id");
        }
    }
    public String leave()
    {
        try
        {
            ws.sendCommand(UserGameCommand.CommandType.LEAVE, authToken, state.currentGameId);
            state.currentGame = null;
            state.perspective = null;
            state.currentGameId = 0;
            state.loggedInState = State.LoggedInState.SIGNEDIN;
        }
        catch (ResponseException e)
        {
            return "Could not leave the game. Please try again.";
        }

        return "You left the game.";
    }
    public String resign()
    {
        try
        {
            System.out.println("Are you sure you would like to resign? Type Y to confirm.");
            String line = scanner.nextLine().toLowerCase();

            if (line.equals("y"))
            {
                ws.sendCommand(UserGameCommand.CommandType.RESIGN, authToken, state.currentGameId);
            }
        }
        catch (ResponseException e)
        {
            return "Could not resign the game. Please try again.";
        }
        return "";
    }
    public String makeMove()
    {
        ChessPosition position = getPosition(SET_TEXT_COLOR_BLUE + "Which piece would you like to move?" +
                " (Use the number to the left of the piece position)", state.perspective);
        ArrayList<ChessMove> moves = new ArrayList<>(state.currentGame.validMoves(position));
        Map<Integer, ChessMove> possibleMoves = new HashMap<>();
        for (int i = 0; i < moves.size(); i++)
        {
            ChessMove move = moves.get(i);
            System.out.println(i+1 + ": " + move.getEndPosition());
            possibleMoves.put(i, move);
        }

        ChessMove selectedMove;
        try
        {
            System.out.println("To which square do you want to move? (Use the number to the left of the piece position)");
            printPrompt();
            selectedMove = possibleMoves.get(Integer.parseInt(scanner.nextLine()) - 1);

            if ((selectedMove.getEndPosition().getRow() == 1 || selectedMove.getEndPosition().getRow() == 8) &&
                    state.currentGame.getBoard().getPiece(new ChessPosition(selectedMove.getStartPosition().getRow(),
                            selectedMove.getEndPosition().getColumn())).getPieceType() == ChessPiece.PieceType.PAWN)
            {
                try
                {
                    System.out.println("Which type would you like to promote to?");
                    System.out.println("1: Queen");
                    System.out.println("2: Rook");
                    System.out.println("3: Bishop");
                    System.out.println("4: Knight");
                    printPrompt();

                    Map<Integer, ChessPiece.PieceType> intToType = Map.of(
                            1, ChessPiece.PieceType.QUEEN,
                            2, ChessPiece.PieceType.ROOK,
                            3, ChessPiece.PieceType.BISHOP,
                            4, ChessPiece.PieceType.KNIGHT
                    );
                    ChessPiece.PieceType type = intToType.get(Integer.parseInt(scanner.nextLine()));

                    selectedMove.setPromotionPiece(type);
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(SET_TEXT_COLOR_RED + "Invalid input. Must be one of the numbers printed above.");
                }
            }
        }
        catch (Exception e)
        {
            return SET_TEXT_COLOR_RED + "Invalid input. Must be one of the numbers printed above.";
        }

        try
        {
            ws.sendMakeMoveCommand(authToken, state.currentGameId, selectedMove);
        }
        catch (ResponseException e)
        {
            return "Could not make move. Please try again.";
        }
        return "";
    }
    private String highlightMoves()
    {
        try
        {
            ChessPosition position = getPosition(
                    "For which piece would you like to  see valid moves? (Use the number to the left of the piece position)",
                    null
            );
            Set<ChessPosition> positions = new HashSet<>();
            for(ChessMove move: state.currentGame.validMoves(position))
            {
                positions.add(move.getEndPosition());
            }
            return printBoard(positions);
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    private ChessPosition getPosition(String message, ChessGame.TeamColor teamColor)
    {
        var positions = state.currentGame.getPiecePositions(teamColor);
        for (int i = 0; i < positions.size(); i++)
        {
            ChessPosition position = positions.get(i);
            System.out.println(i+1 + ": " + position);
        }
        try
        {
            System.out.println(message);
            printPrompt();
            return positions.get(Integer.parseInt(scanner.nextLine()) - 1);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(SET_TEXT_COLOR_RED + "Invalid input. Must be one of the numbers printed above.");
        }
    }
    private String printBoard(Set<ChessPosition> positionsToHighlight)
    {
        new BoardDrawer(state.currentGame.getBoard(), state.perspective, positionsToHighlight).print();
        return "";
    }
    private String help()
    {
        if (state.loggedInState == State.LoggedInState.SIGNEDOUT)
        {
            return """
                    Commands
                    login: Log in an existing user
                    register: Register a new user
                    quit: Exit the chess client""";
        } else if (state.loggedInState == State.LoggedInState.SIGNEDIN)
        {
            return """
                    Commands
                    logout: Logout and return to the prelogin menu
                    create game: Create a new game
                    list games: List all games as well as the participants
                    play game: Join a given game (using the number from "list games")
                    observe game: Watch a given game (using the number from "list games")""";
        } else
        {
            return  """
                    Commands
                    leave: Leave the game
                    redraw: Print out the board
                    highlight moves: Shows all of the valid moves for a selected piece
                    make move: Select a piece and where to move it to
                    resign: resign from and end the game
                    """;
        }
    }
}