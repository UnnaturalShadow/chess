package dataaccess;

import dataaccess.database.DatabaseDaoCollection;
import model.UserData;
//import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTests {

    private UserDao userDao;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseDaoCollection daos = new DatabaseDaoCollection();
        userDao = daos.userDao;
        userDao.clear();
    }

    @Nested
    class CreateAndGetTests {

        @Test
        public void createAndRetrieveUser() throws DataAccessException {
            UserData user = new UserData("Jesus", "password123", "jesus@email.com");
            userDao.createUser(user);

            UserData retrieved = userDao.getUser("Jesus");
            assertNotNull(retrieved);
            assertEquals("Jesus", retrieved.username());
            assertEquals("jesus@email.com", retrieved.email());
        }

        @Test
        public void getNonexistentUser() throws DataAccessException {
            assertNull(userDao.getUser("ghost"));
        }
    }

    @Nested
    class ValidatePasswordTests {

        @Test
        public void correctPasswordReturnsTrue() throws DataAccessException {
            userDao.createUser(new UserData("Peter", "password", "p@email.com"));
            assertTrue(userDao.validateWithPassword("Peter", "password"));
        }

        @Test
        public void incorrectPasswordReturnsFalse() throws DataAccessException {
            userDao.createUser(new UserData("Paul", "pass", "p@email.com"));
            assertFalse(userDao.validateWithPassword("Paul", "wrong"));
        }

        @Test
        public void nonexistentUserReturnsFalse() throws DataAccessException {
            assertFalse(userDao.validateWithPassword("nobody", "password"));
        }
    }

    @Nested
    class ClearTests {

        @Test
        public void clearRemovesAllUsers() throws DataAccessException {
            userDao.createUser(new UserData("A", "x", "a@a.com"));
            userDao.createUser(new UserData("B", "x", "b@b.com"));

            userDao.clear();
            assertNull(userDao.getUser("A"));
            assertNull(userDao.getUser("B"));
        }
    }
}
