package dataaccess;

import dataaccess.exceptions.*;
import model.GameData;
import org.junit.jupiter.api.*;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
import dataaccess.database.DatabaseGameDao;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDaoTests {

    private static GameDao gameDao;

    @BeforeAll
    public static void setup() throws DataAccessException {
        gameDao = new DatabaseGameDao(); // replace with your concrete implementation
        gameDao.clear();
    }

    @Test @Order(1)
    public void create_Positive() throws DataAccessException {
        CreateRequest req = new CreateRequest("Game1");
        int id = gameDao.create(req);
        assertTrue(id > 0, "Game ID should be greater than zero after insertion");
    }

    @Test @Order(2)
    public void create_Negative_NullRequest() {
        assertThrows(DataAccessException.class, () -> gameDao.create(null),
                "Creating a game with a null request should throw DataAccessException");
    }

    @Test @Order(3)
    public void getGame_Positive() throws DataAccessException {
        CreateRequest req = new CreateRequest("Game2");
        int id = gameDao.create(req);
        GameData game = gameDao.getGame(id);
        assertNotNull(game);
        assertEquals("Game2", game.gameName(), "Game name should match the created request");
    }

    @Test @Order(4)
    public void getGame_Negative_InvalidID() throws DataAccessException {
        GameData game = gameDao.getGame(-999);
        assertNull(game, "Requesting a game with invalid ID should return null");
    }

    @Test @Order(5)
    public void list_Positive() throws DataAccessException {
        gameDao.create(new CreateRequest("Game3"));
        List<GameData> games = gameDao.list();
        assertNotNull(games);
        assertFalse(games.isEmpty(), "Game list should contain at least one game");
    }

    @Test @Order(6)
    public void list_Negative_EmptyAfterClear() throws DataAccessException {
        gameDao.clear();
        List<GameData> games = gameDao.list();
        assertEquals(0, games.size(), "Game list should be empty after clear()");
    }

    @Test @Order(7)
    public void join_Positive() throws DataAccessException, AlreadyTakenException {
        int id = gameDao.create(new CreateRequest("Game4"));

        // Always join a free color: "WHITE" or "BLACK" in uppercase to match DAO
        JoinRequest joinReq = new JoinRequest("WHITE", id);

        assertDoesNotThrow(() -> gameDao.join(joinReq, "user1"),
                "Joining a valid game should not throw an exception");
    }

    @Test @Order(8)
    public void join_Negative_AlreadyTaken() throws DataAccessException, AlreadyTakenException {
        int id = gameDao.create(new CreateRequest("Game5"));

        JoinRequest joinReq = new JoinRequest("WHITE", id);
        gameDao.join(joinReq, "user1"); // first player takes the slot

        // second attempt to join same color should fail
        assertThrows(AlreadyTakenException.class, () -> gameDao.join(joinReq, "user2"),
                "Joining the same color slot twice should throw AlreadyTakenException");
    }


    @Test @Order(9)
    public void clear_Positive() throws DataAccessException {
        gameDao.create(new CreateRequest("Game6"));
        gameDao.clear();
        assertEquals(0, gameDao.list().size(), "Game list should be empty after clear()");
    }

    @Test @Order(10)
    public void clear_Negative_NoErrorOnEmptyTable() {
        assertDoesNotThrow(() -> gameDao.clear(), "Clearing an already empty table should not throw");
    }
}
