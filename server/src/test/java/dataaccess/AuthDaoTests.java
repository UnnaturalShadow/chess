package dataaccess;

import dataaccess.database.DatabaseDaoCollection;
//import dataaccess.database.DatabaseAuthDao;
import dataaccess.exceptions.*;
import org.junit.jupiter.api.*;
//import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDaoTests {

    private AuthDao authDao;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseDaoCollection daos = new DatabaseDaoCollection();
        authDao = daos.authDao;
        authDao.clear();
    }

    @Nested
    class AddAndAuthenticateTests {

        @Test
        public void addAndAuthenticateToken() throws DataAccessException {
            authDao.addAuthToken("Jesus", "token123");
            String username = authDao.authenticateToken("token123");

            assertEquals("Jesus", username, "AuthDao should return the correct username for a valid token");
        }

        @Test
        public void authenticateInvalidToken() throws DataAccessException {
            assertNull(authDao.authenticateToken("invalidtoken"), "AuthDao should return null for invalid token");
        }
    }

    @Nested
    class RemoveTests {

        @Test
        public void removeExistingToken() throws DataAccessException {
            authDao.addAuthToken("Jerome", "token1");
            authDao.remove("token1");

            assertNull(authDao.authenticateToken("token1"), "Token should be removed from DB");
        }

        @Test
        public void removeNonexistentToken() {
            assertDoesNotThrow(() -> authDao.remove("notoken"), "Removing nonexistent token should not throw");
        }
    }

    @Nested
    class ClearTests {

        @Test
        public void clearRemovesAllTokens() throws DataAccessException {
            authDao.addAuthToken("A", "t1");
            authDao.addAuthToken("B", "t2");

            authDao.clear();
            assertNull(authDao.authenticateToken("t1"));
            assertNull(authDao.authenticateToken("t2"));
        }
    }
}
