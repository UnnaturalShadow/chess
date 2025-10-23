package service;

import dataaccess.DaoCollection;
import dataaccess.exceptions.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.AuthService;

public class AuthServiceTests
{
    public static AuthService authService;

    @BeforeEach
    public void setup()
    {
        DaoCollection daos = new DaoCollection();
        authService = new AuthService(daos);
        authService.daos.authDao.addAuthToken("Garrett", "dummytoken1");
        authService.daos.authDao.addAuthToken("Jerome", "dummytoken2");
    }

    @Nested
    class LogoutTests
    {
        @Test
        public void successfulLogoutTest()
        {
            Assertions.assertDoesNotThrow(() -> authService.logout("dummytoken1"));
        }

        @Test
        public void unsuccessfulLogoutTest()
        {
            Assertions.assertThrows(Exception.class, () -> authService.logout("dummytoken"));
        }
    }

    @Nested
    class GenerateTokenTests
    {
        @Test
        public void generateTokenWithUsername()
        {
            Assertions.assertNotNull(authService.generateNewToken("Garrett"));
        }

        @Test
        public void generateTokenWithoutUsername()
        {
            Assertions.assertThrows(BadRequestException.class, () -> authService.generateNewToken(null));
        }
    }
}
