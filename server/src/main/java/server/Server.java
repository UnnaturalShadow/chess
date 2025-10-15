package server;

import io.javalin.*;
import com.google.gson.Gson;
import model.*;
import dataaccess.*;
import service.*;
import handler.*;

public class Server
{

    private final Javalin javalin;
    private final Gson gson = new Gson();

    public Server()
    {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var userService = new UserService(userDAO, authDAO);
        var userHandler = new UserHandler(userService);
        // Register your endpoints and exception handlers here.
        javalin.post("/user", userHandler::registerUser);

    }

    public int run(int desiredPort)
    {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
