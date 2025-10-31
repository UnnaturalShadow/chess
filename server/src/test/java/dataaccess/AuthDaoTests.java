package dataaccess;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.database.DatabaseAuthDao;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDaoTests
{

    private static AuthDao authDao;

    @BeforeAll
    public static void setup() throws DataAccessException
    {
        authDao = new DatabaseAuthDao();
        authDao.clear();
    }

    @Test @Order(1)
    public void addAuthTokenPositive() throws DataAccessException
    {
        authDao.addAuthToken("userA", "token123");
        String username = authDao.authenticateToken("token123");
        assertEquals("userA", username);
    }

    @Test @Order(2)
    public void addAuthTokenNegativeDuplicateToken() throws DataAccessException
    {
        authDao.addAuthToken("userB", "dupToken");
        assertThrows(DataAccessException.class, () -> authDao.addAuthToken("userB", "dupToken"));
    }

    @Test @Order(3)
    public void authenticateTokenPositive() throws DataAccessException
    {
        authDao.addAuthToken("userD", "auth123");
        assertEquals("userD", authDao.authenticateToken("auth123"));
    }

    @Test @Order(4)
    public void authenticateTokenNegativeInvalidToken() throws DataAccessException
    {
        assertNull(authDao.authenticateToken("not_a_token"));
    }

    @Test @Order(5)
    public void removePositive() throws DataAccessException
    {
        authDao.addAuthToken("userE", "tokenToRemove");
        authDao.remove("tokenToRemove");
        assertNull(authDao.authenticateToken("tokenToRemove"));
    }

    @Test @Order(6)
    public void removeNegativeNonExistentToken()
    {
        assertDoesNotThrow(() -> authDao.remove("doesNotExist"));
    }

    @Test @Order(7)
    public void clearPositive() throws DataAccessException
    {
        authDao.clear();
        assertNull(authDao.authenticateToken("token123"));
    }

    @Test @Order(8)
    public void clearNegativeNoErrorOnEmptyTable()
    {
        assertDoesNotThrow(() -> authDao.clear());
    }
}
