package dataaccess;

import dataaccess.database.DatabaseDaoCollection;
import dataaccess.exceptions.AlreadyTakenException;
import model.GameData;
import org.junit.jupiter.api.*;
import requestobjects.CreateRequest;
import requestobjects.JoinRequest;
//import dataaccess.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameDaoTests {

    private GameDao gameDao;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseDaoCollection daos = new DatabaseDaoCollection();
        gameDao = daos.gameDao;
        gameDao.clear();
    }

    @Nested
    class CreateAndGetTests {

        @Test
        public void createAndRetrieveGame() throws DataAccessException {
            int gameId = gameDao.create(new CreateRequest("TestGame"));
            GameData retrieved = gameDao.getGame(gameId);

            assertNotNull(retrieved);
            assertEquals("TestGame", retrieved.gameName());
        }

        @Test
        public void getNonexistentGame() throws DataAccessException {
            assertNull(gameDao.getGame(9999));
        }
    }

    @Nested
    class ListTests {

        @Test
        public void listGamesAfterCreation() throws DataAccessException {
            gameDao.create(new CreateRequest("Alpha"));
            gameDao.create(new CreateRequest("Beta"));

            List<GameData> games = gameDao.list();
            assertEquals(2, games.size());
            assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Alpha")));
            assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Beta")));
        }

        @Test
        public void listEmptyWhenCleared() throws DataAccessException {
            gameDao.clear();
            List<GameData> games = gameDao.list();
            assertTrue(games.isEmpty());
        }
    }

    @Nested
    class JoinTests {

        @Test
        public void joinGameSuccessfully() throws DataAccessException {
            int gameId = gameDao.create(new CreateRequest("NewGame"));
            JoinRequest joinReq = new JoinRequest("WHITE", gameId);
            assertDoesNotThrow(() -> gameDao.join(joinReq, "Jesus"));
        }

        @Test
        public void joinSameColorTwiceThrows() throws DataAccessException, AlreadyTakenException {
            int gameId = gameDao.create(new CreateRequest("Game1"));
            JoinRequest joinReq = new JoinRequest("WHITE", gameId);
            gameDao.join(joinReq, "Jesus");

            assertThrows(AlreadyTakenException.class, () -> gameDao.join(joinReq, "Peter"));
        }
    }

    @Nested
    class ClearTests {

        @Test
        public void clearRemovesAllGames() throws DataAccessException {
            gameDao.create(new CreateRequest("X"));
            gameDao.create(new CreateRequest("Y"));

            gameDao.clear();
            assertTrue(gameDao.list().isEmpty());
        }
    }
}
