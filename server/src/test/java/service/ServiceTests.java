package service;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import dataaccess.exceptions.*;
import model.PlayerColor;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.CreateRequest;
import requests.JoinRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    private UserService userService;
    private AuthService authService;
    private GameService gameService;

    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;

    private String token;

    @BeforeEach
    void setup() throws DataAccessException, AlreadyTakenException, MissingFieldException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        authService = new AuthService(authDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        // Seed a user
        userService.register("player", "password", "email@example.com");
        token = authService.generateToken("player");
    }

    // ------------------------
    // UserService tests
    // ------------------------

    @Test
    void userRegisterSuccess() throws Exception {
        var result = userService.register("newUser", "pass", "a@b.com");
        assertNotNull(result.authToken());
    }

    @Test
    void userRegisterDuplicate() {
        assertThrows(AlreadyTakenException.class, () ->
                userService.register("player", "pass", "a@b.com"));
    }

    @Test
    void userLoginSuccess() throws Exception {
        var result = userService.login("player", "password");
        assertEquals("player", result.username());
    }

    @Test
    void userLoginInvalidPassword() {
        assertThrows(InvalidCredentialsException.class, () ->
                userService.login("player", "wrongpass"));
    }

    @Test
    void userClear() throws Exception {
        userService.clear();
        assertThrows(InvalidCredentialsException.class, () ->
                userService.login("player", "password"));
    }

    // ------------------------
    // AuthService tests
    // ------------------------

    @Test
    void authGenerateTokenSuccess() throws Exception {
        String t = authService.generateToken("player");
        assertNotNull(t);
    }

    @Test
    void authLogoutSuccess() throws Exception {
        String t = authService.generateToken("player");
        authService.logout(t);
        assertThrows(DataAccessException.class, () ->
                authService.logout(t));
    }

    @Test
    void authGenerateTokenBlankUsername() {
        assertThrows(DataAccessException.class, () ->
                authService.generateToken(""));
    }

    // ------------------------
    // GameService tests
    // ------------------------

    @Test
    void gameCreateSuccess() throws Exception {
        int id = gameService.create(token, new CreateRequest("My Game"));
        assertTrue(id > 0);
        List<GameData> games = gameService.list(token);
        assertEquals(1, games.size());
        assertEquals("My Game", games.get(0).gameName());
    }

    @Test
    void gameCreateMissingName() {
        assertThrows(MissingFieldException.class, () ->
                gameService.create(token, new CreateRequest(null)));
    }

    @Test
    void gameListInvalidToken() {
        assertThrows(InvalidCredentialsException.class, () ->
                gameService.list("badToken"));
    }

    @Test
    void gameJoinSuccess() throws Exception {
        int id = gameService.create(token, new CreateRequest("My Game"));
        JoinRequest req = new JoinRequest("WHITE", id);
        gameService.join(token, req);

        GameData g = gameDAO.findById(id);
        assertEquals("player", g.whiteUsername());
    }

    @Test
    void gameJoinColorTaken() throws Exception {
        int id = gameService.create(token, new CreateRequest("My Game"));
        JoinRequest req1 = new JoinRequest("WHITE", id);
        gameService.join(token, req1);

        JoinRequest req2 = new JoinRequest("WHITE", id);
        assertThrows(AlreadyTakenException.class, () ->
                gameService.join(token, req2));
    }

    @Test
    void gameJoinInvalidColor() throws Exception {
        int id = gameService.create(token, new CreateRequest("My Game"));
        JoinRequest req = new JoinRequest("PURPLE", id);
        assertThrows(MissingFieldException.class, () ->
                gameService.join(token, req));
    }

    @Test
    void gameJoinNonExistentGame() {
        JoinRequest req = new JoinRequest("WHITE", 999);
        assertThrows(GameNotFoundException.class, () ->
                gameService.join(token, req));
    }
}