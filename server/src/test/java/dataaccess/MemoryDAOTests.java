package dataaccess;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;
import model.UserData;
import org.junit.jupiter.api.Test;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryDAOTests {

    // -----------------------------
    // MemoryAuthDAO Tests
    // -----------------------------
    @Test
    void addTokenPositive() {
        var authDAO = new MemoryAuthDAO();
        authDAO.clear();

        authDAO.addToken("user1", "token1");
        assertEquals("user1", authDAO.findUsernameByToken("token1"));
    }

    @Test
    void addTokenDuplicateOverwrites() {
        var authDAO = new MemoryAuthDAO();
        authDAO.clear();

        authDAO.addToken("user1", "token1");
        authDAO.addToken("user2", "token1"); // MemoryAuthDAO allows overwrite
        assertEquals("user2", authDAO.findUsernameByToken("token1"));
    }

    @Test
    void findUsernameByTokenNonexistentReturnsNull() {
        var authDAO = new MemoryAuthDAO();
        authDAO.clear();

        assertNull(authDAO.findUsernameByToken("missingToken"));
    }

    @Test
    void removeTokenPositive() {
        var authDAO = new MemoryAuthDAO();
        authDAO.clear();

        authDAO.addToken("user1", "token1");
        authDAO.removeToken("token1");
        assertNull(authDAO.findUsernameByToken("token1"));
    }

    // -----------------------------
    // MemoryUserDAO Tests
    // -----------------------------
    @Test
    void saveAndFindByUsernamePositive() {
        var userDAO = new MemoryUserDAO();
        userDAO.clear();

        UserData user = new UserData("alice", "pass123", "alice@test.com");
        userDAO.save(user);

        UserData fetched = userDAO.findByUsername("alice");
        assertEquals("alice", fetched.username());
        assertEquals("alice@test.com", fetched.email());
    }

    @Test
    void findByUsernameNonexistentReturnsNull() {
        var userDAO = new MemoryUserDAO();
        userDAO.clear();

        assertNull(userDAO.findByUsername("bob"));
    }

    @Test
    void validateCredentialsPositive() throws DataAccessException {
        var userDAO = new MemoryUserDAO();
        userDAO.clear();

        UserData user = new UserData("charlie", "secret", "c@test.com");
        userDAO.save(user);

        assertTrue(userDAO.validateCredentials("charlie", "secret"));
    }

    @Test
    void validateCredentialsNegative() {
        var userDAO = new MemoryUserDAO();
        userDAO.clear();

        assertThrows(DataAccessException.class, () ->
                userDAO.validateCredentials("nonexistent", "any"));
    }

    // -----------------------------
    // MemoryGameDAO Tests
    // -----------------------------
    @Test
    void saveAndFindByIdPositive() {
        var gameDAO = new MemoryGameDAO();
        gameDAO.clear();

        GameData game = new GameData(0, null, null, "Chess1", null);
        int id = gameDAO.save(game);

        GameData fetched = gameDAO.findById(id);
        assertEquals("Chess1", fetched.gameName());
    }

    @Test
    void findByIdNonexistentReturnsNull() {
        var gameDAO = new MemoryGameDAO();
        gameDAO.clear();

        assertNull(gameDAO.findById(999));
    }

    @Test
    void findAllReturnsAllGames() {
        var gameDAO = new MemoryGameDAO();
        gameDAO.clear();

        gameDAO.save(new GameData(0, null, null, "G1", null));
        gameDAO.save(new GameData(0, null, null, "G2", null));

        List<GameData> allGames = gameDAO.findAll();
        assertEquals(2, allGames.size());
    }

    @Test
    void assignPlayerPositiveAndAlreadyTaken() throws AlreadyTakenException {
        var gameDAO = new MemoryGameDAO();
        gameDAO.clear();

        int id = gameDAO.save(new GameData(0, null, null, "Chess2", null));

        gameDAO.assignPlayer(id, "player1", PlayerColor.WHITE);
        AlreadyTakenException ex = assertThrows(AlreadyTakenException.class,
                () -> gameDAO.assignPlayer(id, "player2", PlayerColor.WHITE));
        assertTrue(ex.getMessage().contains("White already taken"));
    }

    @Test
    void assignPlayerBlackPositiveAndAlreadyTaken() throws AlreadyTakenException {
        var gameDAO = new MemoryGameDAO();
        gameDAO.clear();

        int id = gameDAO.save(new GameData(0, null, null, "Chess3", null));

        gameDAO.assignPlayer(id, "player1", PlayerColor.BLACK);
        AlreadyTakenException ex = assertThrows(AlreadyTakenException.class,
                () -> gameDAO.assignPlayer(id, "player2", PlayerColor.BLACK));
        assertTrue(ex.getMessage().contains("Black already taken"));
    }
}