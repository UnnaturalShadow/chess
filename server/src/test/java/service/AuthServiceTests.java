package service;

import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.database.DatabaseDaoCollection;
import dataaccess.exceptions.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AuthServiceTests {

    public static AuthService authService;
    private String tokenJesus;
    private String tokenJerome;

    @BeforeEach
    public void setup() throws DataAccessException {
        DaoCollection daos = new DatabaseDaoCollection();
        authService = new AuthService(daos);

        // Generate unique tokens to avoid UNIQUE constraint violation
        tokenJesus = "token_" + System.nanoTime();
        tokenJerome = "token_" + (System.nanoTime() + 1);

        authService.daos.authDao.addAuthToken("Jesus", tokenJesus);
        authService.daos.authDao.addAuthToken("Jerome", tokenJerome);
    }

    @Nested
    class LogoutTests {

        @Test
        public void successfulLogoutTest() {
            Assertions.assertDoesNotThrow(() -> authService.logout(tokenJesus),
                    "Logout with a valid token should not throw an exception");
        }

        @Test
        public void unsuccessfulLogoutTest() {
            Assertions.assertThrows(Exception.class, () -> authService.logout("invalidToken"),
                    "Logout with an invalid token should throw an exception");
        }
    }

    @Nested
    class GenerateTokenTests {

        @Test
        public void generateTokenWithUsername() throws DataAccessException {
            String newToken = authService.generateNewToken("Jesus");
            Assertions.assertNotNull(newToken, "Generated token should not be null");
        }

        @Test
        public void generateTokenWithoutUsername() {
            Assertions.assertThrows(BadRequestException.class, () -> authService.generateNewToken(null),
                    "Generating a token without username should throw BadRequestException");
        }
    }
}
