package ui;

import static ui.EscapeSequences.*;

import chess.ChessGame;
import exception.ResponseException;
import model.GameData;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
import requestobjects.LoginRequest;
import requestobjects.RegisterRequest;
import server.ServerFacade;
import websocket.NotificationHandler;
import websocket.messages.Notification;
import websocket.WebSocketFacade;
import websocket.messages.LoadGame;

import java.util.List;
import java.util.Scanner;

public class ChessClient implements NotificationHandler
{
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;
    Scanner scanner = new Scanner(System.in);
    String authToken;
    List<GameData> games;
    ChessGame currentGame = null;
    ChessGame.TeamColor perspective;

    public ChessClient(String url) throws ResponseException
    {
        server = new ServerFacade(url);
        ws = new WebSocketFacade(url, this);
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
                    if (state == State.SIGNEDOUT)
                    {
                        result = signedOutEval(line);
                    } else if (state == State.SIGNEDIN)
                    {
                        result = signedInEval(line);
                    } else if (state == State.INGAME)
                    {
                        result = inGameEval(line);
                    } else
                    {
                        result = "Unknown command. Type \"help\" to see valid commands.";
                    }
                }
                System.out.println(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e)
            {
                var msg = e.getMessage().replaceFirst(".*Error: ", "");
                System.out.println(msg);
            }
        }
    }

    public void loadGame(LoadGame message)
    {
        currentGame = message.game;
        printBoard();
        printPrompt();
    }

    public void notify(Notification message) {
        System.out.println(SET_TEXT_COLOR_GREEN + message.message);
    }

    private String signedOutEval(String line)
    {
        return switch (line)
        {
            case "help" -> help();
            case "login" -> login();
            case "quit" -> "quit";
            case "register" -> register();
            default -> throw new IllegalStateException("Unexpected value: " + line + " Type help for valid commands.");
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
            default -> throw new IllegalStateException("Unexpected value: " + line + "Type help for valid commands.");
            case "redraw" -> printBoard();
        };
    }

    private void printPrompt()
    {
        System.out.print(RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_RED);
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
            state = State.SIGNEDIN;
            return "Successfully registered.";
        } catch (ResponseException e)
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
            state = State.SIGNEDIN;
            return "Successfully signed in.";
        } catch (ResponseException e)
        {
            return "Failed to sign in with the given credentials.";
        }
    }

    private String logout()
    {
        try
        {
            server.logoutUser(authToken);
            state = State.SIGNEDOUT;
            return "Logging out...";
        } catch (ResponseException e)
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
            games = server.listGame(authToken).games();
            return "Created game \"" + name + "\"";
        } catch (ResponseException e)
        {
            return "Failed to create new game";
        }
    }

    private String listGames()
    {
        try
        {
            games = server.listGame(authToken).games();
            StringBuilder gameStrings = new StringBuilder();
            gameStrings.append("Games\n");

            for (int i = 0; i < games.size(); i++)
            {
                GameData game = games.get(i);
                String gameString = (i+1) + ": " + game.gameName();
                if (game.whiteUsername() != null)
                {gameString += " White: " + game.whiteUsername();}
                if (game.blackUsername() != null)
                {gameString += " Black: " + game.blackUsername();}
                gameStrings.append(gameString).append("\n");
            }

            gameStrings.setLength(gameStrings.length() - 1);
            return gameStrings.toString();

        } catch (ResponseException e)
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
            perspective = ChessGame.TeamColor.WHITE;
            color = "WHITE";
        } else if (line.equalsIgnoreCase("b"))
        {
            perspective = ChessGame.TeamColor.BLACK;
            color = "BLACK";
        } else
        {
            throw new IllegalArgumentException("Can't parse input. Accepted inputs are \"W\" or \"B\"");
        }

        currentGame = gameData.game();
        state = State.INGAME;
        try
        {
            server.joinGame(authToken, new JoinRequest(color, gameData.gameID()));
            games = server.listGame(authToken).games();
            ws.playGame(authToken, gameData.gameID());
        } catch (Exception e)
        {
            return "Could not join game. "+ e.getMessage().replaceFirst(".*Error: ", "");
        }

        return "";
    }

    private String observeGame()
    {
        GameData gameData = getGameFromUser();
        currentGame = gameData.game();
        perspective = ChessGame.TeamColor.WHITE;
        state = State.INGAME;
        return printBoard();
    }

    private GameData getGameFromUser()
    {
        try
        {
            if (games == null)
            {
                games = server.listGame(authToken).games();
            }

            System.out.println("Enter game id");
            printPrompt();
            String line = scanner.nextLine();
            return games.get(Integer.parseInt(line) - 1);
        } catch (Exception e)
        {
            throw new RuntimeException("Could not find game with given id");
        }
    }

    private String printBoard()
    {
        new BoardDrawer(currentGame.getBoard(), perspective).print();
        return "";
    }

    private String help()
    {
        if (state == State.SIGNEDOUT)
        {
            return """
                    Commands
                    login: Log in an existing user
                    register: Register a new user
                    quit: Exit the chess client""";
        } else if (state == State.SIGNEDIN)
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
            return "You are currently in a game. Placeholder text";
        }
    }
}