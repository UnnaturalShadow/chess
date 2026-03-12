package dataaccess;

import dataaccess.database.DatabaseAuthDAO;
import dataaccess.database.DatabaseGameDAO;
import dataaccess.database.DatabaseUserDAO;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseDAOTests {

    // -----------------------------
    // AuthDAO Tests
    // -----------------------------
    @Test
    void addTokenPositive() throws DataAccessException {
        var authDAO = new DatabaseAuthDAO();
        authDAO.clear();

        String username = "user1";
        String token = "token1";

        authDAO.addToken(username, token);

        String result = authDAO.findUsernameByToken(token);
        assertEquals(username, result);
    }

    @Test
    void addTokenDuplicateTokenThrows() throws DataAccessException {
        var authDAO = new DatabaseAuthDAO();
        authDAO.clear();

        String username = "user1";
        String token = "token1";

        authDAO.addToken(username, token);
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> authDAO.addToken("user2", token));
        assertTrue(ex.getMessage().contains("Token already exists"));
    }

    @Test
    void findUsernameByTokenNonexistentReturnsNull() throws DataAccessException {
        var authDAO = new DatabaseAuthDAO();
        authDAO.clear();

        String result = authDAO.findUsernameByToken("nonexistent");
        assertNull(result);
    }

    @Test
    void removeTokenPositive() throws DataAccessException {
        var authDAO = new DatabaseAuthDAO();
        authDAO.clear();

        String username = "user1";
        String token = "token1";

        authDAO.addToken(username, token);
        authDAO.removeToken(token);

        assertNull(authDAO.findUsernameByToken(token));
    }

    // -----------------------------
    // UserDAO Tests
    // -----------------------------
    @Test
    void saveAndFindByUsernamePositive() throws DataAccessException {
        var userDAO = new DatabaseUserDAO();
        userDAO.clear();

        UserData user = new UserData("alice", "password", "alice@test.com");
        userDAO.save(user);

        UserData fetched = userDAO.findByUsername("alice");
        assertEquals("alice", fetched.username());
        assertEquals("alice@test.com", fetched.email());
    }

    @Test
    void findByUsernameNonexistentReturnsNull() throws DataAccessException {
        var userDAO = new DatabaseUserDAO();
        userDAO.clear();

        UserData fetched = userDAO.findByUsername("nonexistent");
        assertNull(fetched);
    }

    @Test
    void validateCredentialsPositiveAndNegative() throws DataAccessException {
        var userDAO = new DatabaseUserDAO();
        userDAO.clear();

        UserData user = new UserData("bob", "secure", "bob@test.com");
        userDAO.save(user);

        assertTrue(userDAO.validateCredentials("bob", "secure"));
        assertFalse(userDAO.validateCredentials("bob", "wrong"));
        assertFalse(userDAO.validateCredentials("nonexistent", "any"));
    }

    // -----------------------------
    // GameDAO Tests
    // -----------------------------
    @Test
    void saveAndFindByIdPositive() throws DataAccessException {
        var gameDAO = new DatabaseGameDAO();
        gameDAO.clear();

        GameData game = new GameData(0, null, null, "ChessGame1", null);
        int id = gameDAO.save(game);

        GameData fetched = gameDAO.findById(id);
        assertEquals("ChessGame1", fetched.gameName());
    }

    @Test
    void findAllReturnsAllGames() throws DataAccessException {
        var gameDAO = new DatabaseGameDAO();
        gameDAO.clear();

        GameData game1 = new GameData(0, null, null, "G1", null);
        GameData game2 = new GameData(0, null, null, "G2", null);
        gameDAO.save(game1);
        gameDAO.save(game2);

        List<GameData> allGames = gameDAO.findAll();
        assertEquals(2, allGames.size());
    }

    @Test
    void assignPlayerPositiveAndAlreadyTaken() throws DataAccessException, AlreadyTakenException {
        var gameDAO = new DatabaseGameDAO();
        gameDAO.clear();

        GameData game = new GameData(0, null, null, "ChessGame2", null);
        int id = gameDAO.save(game);

        gameDAO.assignPlayer(id, "player1", PlayerColor.WHITE);

        // Trying to assign again should throw
        AlreadyTakenException ex = assertThrows(AlreadyTakenException.class,
                () -> gameDAO.assignPlayer(id, "player2", PlayerColor.WHITE));
        assertTrue(ex.getMessage().contains("WHITE player already assigned"));
    }
}
