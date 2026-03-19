package ui;

import java.util.*;




public class ChessClient
{

    Scanner scanner = new Scanner(System.in);
    int[] state = {0,0};

    public void start()
    {
        System.out.println("Welcome to 240 chess. Type help to begin.");

        var input = "";
        while (!input.equals("quit"))
        {
            prompt();
            input = scanner.nextLine().toLowerCase();

            switch(input)
            {
                case "help":
                    System.out.println(help());
                    break;

                case "register":
                    if(state[0] == 0)
                    {
                        register();
                    }
                    else
                    {
                        System.out.println("Invalid command. Type \"help\" to see valid commands");
                    }
                    break;
                case "quit":
                    break;
            }
        }
    }

    public void register()
    {
        System.out.println("Please enter your username");
        prompt();
        String username = scanner.nextLine();
        System.out.println("Please enter your password");
        prompt();
        String password = scanner.nextLine();
        System.out.println("Please enter your email");
        prompt();
        String email = scanner.nextLine();

        try
        {
            //call to server to register. If successful
            state[0] = 1;
            System.out.println("Successfully registered");
        }
        catch (Exception e)
        {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    public String help()
    {
        if(state[0] == 1)
        {
            return """
                    register -  register a new user
                    login - login an existing user
                    quit - to quit the program
                    help - see possible commands
                    """;
        }
        return "";
    }

    public void prompt()
    {
        System.out.print(EscapeSequences.RESET_TEXT_COLOR + ">>> " + EscapeSequences.SET_TEXT_COLOR_BLUE);
    }


}
