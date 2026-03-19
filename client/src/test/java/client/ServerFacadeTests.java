package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import exception.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    @BeforeEach
    public void clearDB() throws Exception {
        facade.clear();
    }

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // =========================
    // REGISTER
    // =========================

    @Test
    public void registerPositive() throws Exception {
        AuthData auth = facade.register("userA", "password", "email@test.com");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("userA", auth.username());
    }

    @Test
    public void registerNegative_duplicateUser() throws Exception {
        facade.register("userB", "password", "email@test.com");

        assertThrows(ResponseException.class, () -> {
            facade.register("userB", "password", "email@test.com");
        });
    }

    // =========================
    // LOGIN
    // =========================

    @Test
    public void loginPositive() throws Exception {
        facade.register("userC", "password", "email@test.com");

        AuthData auth = facade.login("userC", "password");

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("userC", auth.username());
    }

    @Test
    public void loginNegative_wrongPassword() throws Exception {
        facade.register("userD", "password", "email@test.com");

        assertThrows(ResponseException.class, () -> {
            facade.login("userD", "wrongPassword");
        });
    }

    // =========================
    // LOGOUT
    // =========================

    @Test
    public void logoutPositive() throws Exception {
        AuthData auth = facade.register("userE", "password", "email@test.com");

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegative_invalidToken() {
        assertThrows(ResponseException.class, () -> {
            facade.logout("bad-token");
        });
    }

    // =========================
    // CREATE GAME
    // =========================

    @Test
    public void createGamePositive() throws Exception {
        AuthData auth = facade.register("userF", "password", "email@test.com");

        int gameID = facade.createGame(auth.authToken(), "My Game");

        assertTrue(gameID > 0);
    }

    @Test
    public void createGameNegative_noAuth() {
        assertThrows(ResponseException.class, () -> {
            facade.createGame(null, "Game");
        });
    }

    // =========================
    // LIST GAMES
    // =========================

    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth = facade.register("userG", "password", "email@test.com");

        facade.createGame(auth.authToken(), "Game1");

        GameData[] games = facade.listGames(auth.authToken());

        assertNotNull(games);
        assertTrue(games.length >= 1);
    }

    @Test
    public void listGamesNegative_badAuth() {
        assertThrows(ResponseException.class, () -> {
            facade.listGames("bad-token");
        });
    }

    // =========================
    // JOIN GAME
    // =========================

    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth = facade.register("userH", "password", "email@test.com");

        int gameID = facade.createGame(auth.authToken(), "Joinable");

        assertDoesNotThrow(() -> {
            facade.joinGame(auth.authToken(), gameID, "WHITE");
        });
    }

    @Test
    public void joinGameNegative_invalidGame() throws Exception {
        AuthData auth = facade.register("userI", "password", "email@test.com");

        assertThrows(ResponseException.class, () -> {
            facade.joinGame(auth.authToken(), 999999, "BLACK");
        });
    }
}