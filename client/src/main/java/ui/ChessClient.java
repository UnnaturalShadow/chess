package ui;

import java.util.*;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import client.ServerFacade;




public class ChessClient
{

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);
    private ChessGame game;

    private AuthData auth = null;
    private List<GameData> lastGameList = new ArrayList<>();

    public ChessClient(ServerFacade server)
    {
        this.server = server;
    }

    public void run()
    {
        System.out.println("Welcome to Chess!");

        while (true)
        {
            try
            {
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                if (auth == null)
                {
                    handlePrelogin(input);
                }
                else
                {
                    handlePostlogin(input);
                }
            }
            catch (Exception e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }



    private void handlePrelogin(String input) throws ResponseException
    {
        String[] parts = input.split(" ");

        switch (parts[0].toLowerCase())
        {
            case "help" -> help();

            case "quit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }

            case "login" -> {
                if (parts.length < 3)
                {
                    System.out.println("Usage: login <username> <password>");
                    return;
                }

                auth = server.login(parts[1], parts[2]);
                System.out.println("Logged in as " + auth.username());
            }

            case "register" -> {
                if (parts.length < 4)
                {
                    System.out.println("Usage: register <username> <password> <email>");
                    return;
                }

                auth = server.register(parts[1], parts[2], parts[3]);
                System.out.println("Registered and logged in as " + auth.username());
            }

            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }


    private void handlePostlogin(String input) throws ResponseException
    {
        String[] parts = input.split(" ");

        switch (parts[0].toLowerCase())
        {
            case "help" -> help();

            case "logout" -> {
                server.logout(auth.authToken());
                auth = null;
                System.out.println("Logged out.");
            }

            case "create" -> {
                if (parts.length < 2)
                {
                    System.out.println("Usage: create <gameName>");
                    return;
                }

                String name = input.substring(input.indexOf(" ") + 1);
                int id = server.createGame(auth.authToken(), name);
                System.out.println("Created game with ID: " + id);
            }

            case "list" -> {
                GameData[] games = server.listGames(auth.authToken());
                lastGameList = Arrays.asList(games);

                if (games.length == 0)
                {
                    System.out.println("No games available.");
                    return;
                }

                for (int i = 0; i < games.length; i++)
                {
                    var g = games[i];
                    System.out.printf("%d. %s (White: %s, Black: %s)%n",
                            i + 1,
                            g.gameName(),
                            g.whiteUsername(),
                            g.blackUsername());
                }
            }

            case "play" -> {
                if (parts.length < 3)
                {
                    System.out.println("Usage: play <number> <WHITE|BLACK>");
                    return;
                }

                int index = Integer.parseInt(parts[1]) - 1;
                String color = parts[2].toUpperCase();

                if (!validIndex(index))
                {
                    return;
                }

                int gameID = lastGameList.get(index).gameID();
                game = lastGameList.get(index).game();
                server.joinGame(auth.authToken(), gameID, color);

                System.out.println("Joined game as " + color);
                drawBoard(color.equals("BLACK"));
            }

            case "observe" -> {
                if (parts.length < 2)
                {
                    System.out.println("Usage: observe <number>");
                    return;
                }

                int index = Integer.parseInt(parts[1]) - 1;

                if (!validIndex(index)) return;

                int gameID = lastGameList.get(index).gameID();
                server.joinGame(auth.authToken(), gameID, null);

                System.out.println("Observing game");
                drawBoard(false); // observers see white perspective
            }

            case "quit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }

            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    private boolean validIndex(int index)
    {
        if (index < 0 || index >= lastGameList.size())
        {
            System.out.println("Invalid game number.");
            return false;
        }
        return true;
    }

    private void help()
    {
        if (auth == null)
        {
            System.out.println("""
                Commands:
                  help
                  login <username> <password>
                  register <username> <password> <email>
                  quit
                """);
        }
        else
        {
            System.out.println("""
                Commands:
                  help
                  create <gameName>
                  list
                  play <number> <WHITE|BLACK>
                  observe <number>
                  logout
                  quit
                """);
        }
    }

    private void drawBoard(boolean blackPerspective)
    {
        ChessBoardPrinter printer = new ChessBoardPrinter(game);
        printer.printBoard(blackPerspective);
    }
}

