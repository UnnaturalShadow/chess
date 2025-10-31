package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.database.DatabaseUserDao;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoTests {

    private static UserDao userDao;

    @BeforeAll
    public static void setup() throws DataAccessException {
        userDao = new DatabaseUserDao();
        userDao.clear();
    }

    @Test @Order(1)
    public void createUser_Positive() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@email.com");
        userDao.createUser(user);

        UserData retrieved = userDao.getUser("alice");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
    }

    @Test @Order(2)
    public void createUser_Negative_DuplicateUsername() throws DataAccessException {
        UserData user = new UserData("bob", "pass", "b@email.com");
        userDao.createUser(user);
        assertThrows(DataAccessException.class, () -> userDao.createUser(user));
    }

    @Test @Order(3)
    public void getUser_Positive() throws DataAccessException {
        UserData expected = new UserData("charlie", "charpass", "charlie@email.com");
        userDao.createUser(expected);

        UserData actual = userDao.getUser("charlie");
        assertEquals(expected.username(), actual.username());
    }

    @Test @Order(4)
    public void getUser_Negative_NotFound() throws DataAccessException {
        assertNull(userDao.getUser("nonexistent_user"));
    }

    @Test @Order(5)
    public void validateWithPassword_Positive() throws DataAccessException {
        userDao.createUser(new UserData("dan", "secret", "d@email.com"));
        assertTrue(userDao.validateWithPassword("dan", "secret"));
    }

    @Test @Order(6)
    public void validateWithPassword_Negative_WrongPassword() throws DataAccessException {
        userDao.createUser(new UserData("eve", "goodpass", "e@email.com"));
        assertFalse(userDao.validateWithPassword("eve", "wrongpass"));
    }

    @Test @Order(7)
    public void clear_Positive() throws DataAccessException {
        userDao.clear();
        assertNull(userDao.getUser("alice"));
    }

    @Test @Order(8)
    public void clear_Negative_NoErrorOnEmptyTable() {
        assertDoesNotThrow(() -> userDao.clear());
    }
}
