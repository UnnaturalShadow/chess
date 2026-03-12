package dataaccess;

import chess.ChessGame;
import dataaccess.database.DatabaseAuthDAO;
import dataaccess.database.DatabaseGameDAO;
import dataaccess.database.DatabaseUserDAO;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import model.PlayerColor;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseDAOTests {

    // -------------------------
    // AUTH DAO TESTS
    // -------------------------
    @Nested
    class AuthDAOTests {

        private DatabaseAuthDAO authDAO;

        @BeforeEach
        void setup() throws DataAccessException {
            authDAO = new DatabaseAuthDAO();
            authDAO.clear();
        }

        @Test
        void addToken_positive() throws DataAccessException {
            authDAO.addToken("user1", "token1");
            String username = authDAO.findUsernameByToken("token1");
            assertEquals("user1", username);
        }

        @Test
        void addToken_duplicateToken_throws() {
            assertThrows(DataAccessException.class, () -> {
                authDAO.addToken("user1", "token1");
                authDAO.addToken("user2", "token1");
            });
        }

        @Test
        void findUsernameByToken_nonexistent_returnsNull() throws DataAccessException {
            assertNull(authDAO.findUsernameByToken("noToken"));
        }

        @Test
        void removeToken_positive() throws DataAccessException {
            authDAO.addToken("user1", "token1");
            authDAO.removeToken("token1");
            assertNull(authDAO.findUsernameByToken("token1"));
        }
    }

    // -------------------------
    // USER DAO TESTS
    // -------------------------
    @Nested
    class UserDAOTests {

        private DatabaseUserDAO userDAO;

        @BeforeEach
        void setup() throws DataAccessException {
            userDAO = new DatabaseUserDAO();
            userDAO.clear();
        }

        @Test
        void saveAndFindByUsername_positive() throws DataAccessException {
            UserData user = new UserData("user1", "pass1", "email@example.com");
            userDAO.save(user);

            UserData fetched = userDAO.findByUsername("user1");
            assertNotNull(fetched);
            assertEquals("user1", fetched.username());
            assertEquals("email@example.com", fetched.email());
            assertNotEquals("pass1", fetched.password()); // password is hashed
        }

        @Test
        void findByUsername_nonexistent_returnsNull() throws DataAccessException {
            assertNull(userDAO.findByUsername("noUser"));
        }

        @Test
        void validateCredentials_positiveAndNegative() throws DataAccessException {
            UserData user = new UserData("user1", "pass1", "email@example.com");
            userDAO.save(user);

            assertTrue(userDAO.validateCredentials("user1", "pass1"));
            assertFalse(userDAO.validateCredentials("user1", "wrong"));
            assertFalse(userDAO.validateCredentials("noUser", "pass1"));
        }
    }

    // -------------------------
    // GAME DAO TESTS
    // -------------------------
    @Nested
    class GameDAOTests {

        private DatabaseGameDAO gameDAO;

        @BeforeEach
        void setup() throws DataAccessException {
            gameDAO = new DatabaseGameDAO();
            gameDAO.clear();
        }

        @Test
        void saveAndFindById_positive() throws DataAccessException {
            GameData game = new GameData(0, null, null, "Game1", new ChessGame());
            int id = gameDAO.save(game);

            GameData fetched = gameDAO.findById(id);
            assertNotNull(fetched);
            assertEquals("Game1", fetched.gameName());
        }

        @Test
        void findAll_returnsAllGames() throws DataAccessException {
            gameDAO.save(new GameData(0, null, null, "Game1", new ChessGame()));
            gameDAO.save(new GameData(0, null, null, "Game2", new ChessGame()));

            List<GameData> games = gameDAO.findAll();
            assertEquals(2, games.size());
        }

        @Test
        void assignPlayer_positiveAndAlreadyTaken() throws DataAccessException, AlreadyTakenException {
            int id = gameDAO.save(new GameData(0, null, null, "Game1", new ChessGame()));

            gameDAO.assignPlayer(id, "Alice", PlayerColor.WHITE);
            assertThrows(AlreadyTakenException.class,
                    () -> gameDAO.assignPlayer(id, "Bob", PlayerColor.WHITE));
        }
    }
}