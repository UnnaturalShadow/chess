package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.database.DatabaseUserDao;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTests
{

    private static UserDao userDao;

    @BeforeAll
    public static void setup() throws DataAccessException
    {
        userDao = new DatabaseUserDao();
        userDao.clear();
    }

    @Test @Order(1)
    public void createUserPositive() throws DataAccessException
    {
        UserData user = new UserData("alice", "password123", "alice@email.com");
        userDao.createUser(user);

        UserData retrieved = userDao.getUser("alice");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
    }

    @Test @Order(2)
    public void createUserNegativeDuplicateUsername() throws DataAccessException
    {
        UserData user = new UserData("bob", "pass", "b@email.com");
        userDao.createUser(user);
        assertThrows(DataAccessException.class, () -> userDao.createUser(user));
    }

    @Test @Order(3)
    public void getUserPositive() throws DataAccessException
    {
        UserData expected = new UserData("charlie", "charpass", "charlie@email.com");
        userDao.createUser(expected);

        UserData actual = userDao.getUser("charlie");
        assertEquals(expected.username(), actual.username());
    }

    @Test @Order(4)
    public void getUserNegativeNotFound() throws DataAccessException
    {
        assertNull(userDao.getUser("nonexistent_user"));
    }

    @Test @Order(5)
    public void validateWithPasswordPositive() throws DataAccessException
    {
        userDao.createUser(new UserData("dan", "secret", "d@email.com"));
        assertTrue(userDao.validateWithPassword("dan", "secret"));
    }

    @Test @Order(6)
    public void validateWithPasswordNegativeWrongPassword() throws DataAccessException
    {
        userDao.createUser(new UserData("eve", "goodpass", "e@email.com"));
        assertFalse(userDao.validateWithPassword("eve", "wrongpass"));
    }

    @Test @Order(7)
    public void clearPositive() throws DataAccessException
    {
        userDao.clear();
        assertNull(userDao.getUser("alice"));
    }

    @Test @Order(8)
    public void clearNegativeNoErrorOnEmptyTable()
    {
        assertDoesNotThrow(() -> userDao.clear());
    }
}
