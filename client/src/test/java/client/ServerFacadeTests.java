
package client;

import dataaccess.DataAccessException;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import requestobjects.*;
import server.Server;
import server.ServerFacade;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        String url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void clearDb() throws DataAccessException {
        server.daos.userDao.clear();
        server.daos.gameDao.clear();
        server.daos.authDao.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void createUserTest() throws ResponseException {
        RegisterRequest request = new RegisterRequest("Jesus", "password", "Jesus@email.com");

        Assertions.assertEquals("Jesus", serverFacade.createUser(request).username());
    }

    @Test
    public void createDuplicateUserTest() throws ResponseException {
        RegisterRequest request = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        serverFacade.createUser(request);
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.createUser(request));
    }

    @Test
    public void loginUserTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        serverFacade.createUser(rRequest);

        LoginRequest lRequest = new LoginRequest("Jesus", "password");
        Assertions.assertNotNull(serverFacade.loginUser(lRequest).authToken());
    }

    @Test
    public void loginInvalidUserTest() {
        LoginRequest lRequest = new LoginRequest("Jesus", "password");
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.loginUser(lRequest));
    }

    @Test
    public void logoutUserTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        String token = serverFacade.createUser(rRequest).authToken();

        Assertions.assertDoesNotThrow(() -> serverFacade.logoutUser(token));
    }

    @Test
    public void logoutWithInvalidTokenUserTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.logoutUser("Invalid"));
    }

    @Test
    public void createGameTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        String token = serverFacade.createUser(rRequest).authToken();

        CreateRequest cRequest = new CreateRequest("game");
        serverFacade.createGame(token, cRequest);

        Assertions.assertDoesNotThrow(() -> serverFacade.logoutUser(token));
    }

    @Test
    public void createInvalidGameTest() {
        CreateRequest cRequest = new CreateRequest("game");
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.createGame("Invalid", cRequest));
    }

    @Test
    public void logoutInvalidUserTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.logoutUser("NonsenseToken"));
    }

    @Test
    public void listGameTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        String token = serverFacade.createUser(rRequest).authToken();

        CreateRequest cRequest = new CreateRequest("game");
        serverFacade.createGame(token, cRequest);

        Assertions.assertNotNull(serverFacade.listGame(token).games());
    }

    @Test
    public void listWithInvalidTokenGameTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.listGame("Invalid"));
    }

    @Test
    public void joinGameTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        String token = serverFacade.createUser(rRequest).authToken();

        CreateRequest cRequest = new CreateRequest("game");
        serverFacade.createGame(token, cRequest);

        JoinRequest jRequest = new JoinRequest("WHITE", 1);
        Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(token, jRequest));
    }

    @Test
    public void joinWithInvalidTokenGameTest() {
        JoinRequest jRequest = new JoinRequest("WHITE", 1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.joinGame("Invalid", jRequest));
    }

    @Test
    public void listWithInvalidTokenGame() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.listGame("Invalid"));
    }

    @Test
    public void clearDbTest() throws ResponseException {
        RegisterRequest rRequest = new RegisterRequest("Jesus", "password", "Jesus@email.com");
        String token = serverFacade.createUser(rRequest).authToken();

        serverFacade.clearDb();
        Assertions.assertThrows(IllegalArgumentException.class, () -> serverFacade.listGame(token));
    }
}
